package cv.terrasystem.zebratravelb.hotel;

import cv.terrasystem.zebratravelb.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "hotel_reservations")
@Getter
@Setter
@NoArgsConstructor
public class HotelReservation {

    public static final String ONLINE = "ONLINE";
    public static final String TRANSFER = "TRANSFER";
    public static final String CASH = "CASH";

    public static final String PENDING_PAYMENT = "PENDING_PAYMENT";
    public static final String AWAITING_TRANSFER = "AWAITING_TRANSFER";
    public static final String AWAITING_CASH = "AWAITING_CASH";
    public static final String ON_HOLD = "ON_HOLD";
    public static final String CONFIRMED = "CONFIRMED";
    public static final String PAID = "PAID";
    public static final String CANCELLED = "CANCELLED";
    public static final String FAILED = "FAILED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "room_id")
    private HotelRoom room;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "guest_name")
    private String guestName;
    @Column(name = "guest_email")
    private String guestEmail;
    @Column(name = "guest_phone")
    private String guestPhone;

    @Column(name = "check_in", nullable = false)
    private LocalDate checkIn;

    @Column(name = "check_out", nullable = false)
    private LocalDate checkOut;

    @Column(nullable = false)
    private Integer guests = 1;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod;

    @Column(nullable = false)
    private String status = PENDING_PAYMENT;

    @Column(name = "merchant_ref")
    private String merchantRef;

    @Column(name = "merchant_session")
    private String merchantSession;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "checked_in_at")
    private LocalDateTime checkedInAt;

    @Column(name = "checked_out_at")
    private LocalDateTime checkedOutAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
