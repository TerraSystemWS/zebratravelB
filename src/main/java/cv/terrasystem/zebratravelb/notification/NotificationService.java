package cv.terrasystem.zebratravelb.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

// Regista uma ligação SSE por cada ADMIN/AGENTE com o ZebraDash aberto e empurra um evento
// para todas de cada vez que notify() é chamado a partir de um dos controllers que geram
// notificações (reservas, mensagens, comentários, candidaturas). Registo em memória — perde-se
// ao reiniciar o backend, o que é aceitável: o EventSource do browser religa-se sozinho.
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(0L);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));
        emitters.add(emitter);
        try {
            emitter.send(SseEmitter.event().name("connected").data("ok"));
        } catch (IOException e) {
            emitters.remove(emitter);
        }
        return emitter;
    }

    public void notify(String type, String title, String body, String linkUrl, Integer relatedEntityId) {
        Notification notification = new Notification();
        notification.setType(type);
        notification.setTitle(title);
        notification.setBody(body);
        notification.setLinkUrl(linkUrl);
        notification.setRelatedEntityId(relatedEntityId);
        Notification saved = notificationRepository.save(notification);

        NotificationDto dto = NotificationDto.from(saved);
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("notification").data(dto, MediaType.APPLICATION_JSON));
            } catch (IOException | IllegalStateException e) {
                emitters.remove(emitter);
            }
        }
    }

    // Histórico apagado ao fim de 24h (pedido do utilizador) — busca-e-apaga em vez de um
    // deleteBy...() derivado, que não é automaticamente transacional fora de um contexto já
    // transacional (mesma classe de bug já apanhada nesta base de código, ver dev-notes.md).
    @Scheduled(fixedRate = 3_600_000)
    public void cleanupOldNotifications() {
        List<Notification> old = notificationRepository.findByCreatedAtBefore(LocalDateTime.now().minusHours(24));
        if (!old.isEmpty()) {
            notificationRepository.deleteAll(old);
        }
    }
}
