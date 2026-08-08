package cv.terrasystem.zebratravelb.subscriber;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "subscribers")
@Getter
@Setter
@NoArgsConstructor
public class Subscriber {

    // Placeholder até a verificação de conectividade real ser implementada (ver tarefas.md) —
    // por agora todo subscritor fica "PENDING" indefinidamente, nunca é alterado pelo código.
    public static final String PENDING = "PENDING";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "subscribed_at", nullable = false, updatable = false)
    private LocalDateTime subscribedAt;

    @Column(nullable = false)
    private String status = PENDING;

    @PrePersist
    void onCreate() {
        subscribedAt = LocalDateTime.now();
    }
}
