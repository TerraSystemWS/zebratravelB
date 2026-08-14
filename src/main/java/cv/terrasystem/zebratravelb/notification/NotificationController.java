package cv.terrasystem.zebratravelb.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
public class NotificationController {

    private static final int RECENT_LIMIT = 50;

    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    // Autenticado pelo JwtAuthFilter a partir de ?token=, exceção só para esta rota — a API
    // EventSource do browser não permite definir o header Authorization (ver dev-notes.md).
    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        return notificationService.subscribe();
    }

    @GetMapping
    public List<NotificationDto> getAll() {
        return notificationRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, RECENT_LIMIT))
                .stream().map(NotificationDto::from).toList();
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount() {
        return Map.of("count", notificationRepository.countByReadFalse());
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllRead() {
        List<Notification> unread = notificationRepository.findByReadFalse();
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
        return ResponseEntity.noContent().build();
    }
}
