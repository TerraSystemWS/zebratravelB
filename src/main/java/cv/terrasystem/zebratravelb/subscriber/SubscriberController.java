package cv.terrasystem.zebratravelb.subscriber;

import cv.terrasystem.zebratravelb.common.BadRequestException;
import cv.terrasystem.zebratravelb.common.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/subscribers")
@RequiredArgsConstructor
public class SubscriberController {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final SubscriberRepository subscriberRepository;
    private final SubscriberService subscriberService;

    // Achado ao investigar um bug idêntico em ContactMessageController: "void" devolve 200 com
    // corpo vazio, e o cliente público (zebratravel/lib/api.ts) tenta sempre um res.json(), que
    // rebenta em corpo vazio — a subscrição à newsletter ficava sempre a mostrar erro ao
    // visitante mesmo quando o email era guardado com sucesso.
    @PostMapping
    public ResponseEntity<Void> subscribe(@RequestBody SubscribeRequest request) {
        String email = request.email() != null ? request.email().trim().toLowerCase() : null;
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new BadRequestException("Email inválido");
        }
        subscriberService.subscribeIfAbsent(email);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<SubscriberDto> getAll() {
        return subscriberRepository.findAll().stream().map(SubscriberDto::from).toList();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (!subscriberRepository.existsById(id)) {
            throw new NotFoundException("Subscritor não encontrado: " + id);
        }
        subscriberRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
