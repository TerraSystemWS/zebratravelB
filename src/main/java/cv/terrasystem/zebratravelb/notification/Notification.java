package cv.terrasystem.zebratravelb.notification;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
public class Notification {

    public static final String HOTEL_RESERVATION = "HOTEL_RESERVATION";
    public static final String EXCURSION_BOOKING = "EXCURSION_BOOKING";
    public static final String CONTACT_MESSAGE = "CONTACT_MESSAGE";
    public static final String REVIEW = "REVIEW";
    public static final String JOB_APPLICATION = "JOB_APPLICATION";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(name = "link_url")
    private String linkUrl;

    @Column(name = "related_entity_id")
    private Integer relatedEntityId;

    // Estado partilhado, não por utilizador — mesmo modelo já usado em ContactMessage.read:
    // qualquer ADMIN/AGENTE que marque como lida marca para todos.
    @Column(nullable = false)
    private boolean read = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
