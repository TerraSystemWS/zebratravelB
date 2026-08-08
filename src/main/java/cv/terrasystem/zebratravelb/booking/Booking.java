package cv.terrasystem.zebratravelb.booking;

import cv.terrasystem.zebratravelb.excursion.Excursion;
import cv.terrasystem.zebratravelb.tour.Tour;
import cv.terrasystem.zebratravelb.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "excursion_id")
    private Excursion excursion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tour_id")
    private Tour tour;

    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;

    @Column(nullable = false)
    private String status = "PENDING";

    // Separado de `status` (workflow: pendente/confirmada/cancelada) — responde só
    // à pergunta "esta reserva já foi paga?", já que Excursion/Tour não têm pagamento
    // online integrado hoje (ao contrário de Hotel/Loja), é o admin/agente que marca.
    @Column(name = "payment_status", nullable = false)
    private String paymentStatus = "UNPAID";

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
