package cv.terrasystem.zebratravelb.booking;

import cv.terrasystem.zebratravelb.excursion.Excursion;
import cv.terrasystem.zebratravelb.excursion.ExcursionGroup;
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

    public static final String ONLINE = "ONLINE";
    public static final String TRANSFER = "TRANSFER";
    public static final String CASH = "CASH";

    public static final String PENDING = "PENDING";
    public static final String PENDING_PAYMENT = "PENDING_PAYMENT";
    public static final String AWAITING_TRANSFER = "AWAITING_TRANSFER";
    public static final String AWAITING_CASH = "AWAITING_CASH";
    // "CONFIRMED" já significa "confirmada e paga" — mesmo padrão simplificado de HotelReservation.
    public static final String CONFIRMED = "CONFIRMED";
    public static final String CANCELLED = "CANCELLED";
    public static final String FAILED = "FAILED";

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
    @JoinColumn(name = "excursion_group_id")
    private ExcursionGroup excursionGroup;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tour_id")
    private Tour tour;

    @Column(name = "item_name", nullable = false)
    private String itemName;

    // Preenchidos quando o participante é adicionado manualmente por ADMIN/AGENTE (Grupo Travel
    // do dashboard) e não tem conta de cliente — mesmo padrão de HotelReservation.guestName/etc.
    @Column(name = "guest_name")
    private String guestName;
    @Column(name = "guest_email")
    private String guestEmail;
    @Column(name = "guest_phone")
    private String guestPhone;

    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;

    @Column(nullable = false)
    private String status = PENDING;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    // Preenchidos só para reservas de Excursão feitas pelo cliente (ver BookingController.create) —
    // participantes de Grupo Travel adicionados manualmente pelo admin/agente não passam pelo
    // gateway, ficam sempre null aqui (mesmo padrão de HotelReservation, secção 3/8 do dev-notes).
    @Column(name = "payment_method")
    private String paymentMethod;
    @Column(name = "merchant_ref")
    private String merchantRef;
    @Column(name = "merchant_session")
    private String merchantSession;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
