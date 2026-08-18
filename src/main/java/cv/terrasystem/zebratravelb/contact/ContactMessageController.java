package cv.terrasystem.zebratravelb.contact;

import cv.terrasystem.zebratravelb.common.BadRequestException;
import cv.terrasystem.zebratravelb.common.NotFoundException;
import cv.terrasystem.zebratravelb.notification.Notification;
import cv.terrasystem.zebratravelb.notification.NotificationService;
import cv.terrasystem.zebratravelb.security.turnstile.TurnstileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// Mensagens submetidas pelo formulário de Contacto do site público — inbox simples no
// ZebraDash (ver dev-notes.md). Padrão igual a SubscriberController/JobApplicationController:
// POST público, leituras/gestão só para ADMIN/AGENTE.
@RestController
@RequestMapping("/api/contact-messages")
@RequiredArgsConstructor
public class ContactMessageController {

    private final ContactMessageRepository repository;
    private final NotificationService notificationService;
    private final TurnstileService turnstileService;

    // ResponseEntity.noContent() (204) em vez de "void" — um método void devolve 200 com corpo
    // vazio, e o cliente (zebratravel/lib/api.ts) tenta sempre fazer res.json() a não ser que o
    // status seja exatamente 204, rebentando com "Unexpected end of JSON input" apesar de a
    // mensagem já ter sido gravada com sucesso.
    @PostMapping
    public ResponseEntity<Void> create(@RequestBody CreateContactMessageRequest request) {
        turnstileService.verify(request.turnstileToken());
        if (request.name() == null || request.name().isBlank()) {
            throw new BadRequestException("Nome é obrigatório");
        }
        if (request.email() == null || request.email().isBlank()) {
            throw new BadRequestException("Email é obrigatório");
        }
        if (request.message() == null || request.message().isBlank()) {
            throw new BadRequestException("Mensagem é obrigatória");
        }
        ContactMessage message = new ContactMessage();
        message.setName(request.name());
        message.setEmail(request.email());
        message.setPhone(request.phone());
        message.setSubject(request.subject());
        message.setMessage(request.message());
        ContactMessage saved = repository.save(message);
        notificationService.notify(Notification.CONTACT_MESSAGE, "Nova mensagem de contacto",
                saved.getName() + (saved.getSubject() != null && !saved.getSubject().isBlank() ? " — " + saved.getSubject() : ""),
                "/dashboard/contacts", saved.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public List<ContactMessageDto> getAll() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream().map(ContactMessageDto::from).toList();
    }

    @GetMapping("/unread-count")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public Map<String, Long> unreadCount() {
        return Map.of("count", repository.countByReadFalse());
    }

    @PatchMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public ContactMessageDto markRead(@PathVariable Integer id) {
        ContactMessage message = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Mensagem não encontrada: " + id));
        message.setRead(true);
        return ContactMessageDto.from(repository.save(message));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Mensagem não encontrada: " + id);
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
