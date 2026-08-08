package cv.terrasystem.zebratravelb.excursion;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "excursion_groups")
@Getter
@Setter
@NoArgsConstructor
public class ExcursionGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "excursion_id", nullable = false)
    private Excursion excursion;

    // OPEN = a receber reservas; CONFIRMED = data final marcada (alimenta a home);
    // COMPLETED = já realizada, fica só no histórico.
    @Column(nullable = false)
    private String status = "OPEN";

    @Column(name = "confirmed_date")
    private LocalDate confirmedDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
