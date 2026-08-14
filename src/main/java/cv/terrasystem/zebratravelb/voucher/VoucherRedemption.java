package cv.terrasystem.zebratravelb.voucher;

import cv.terrasystem.zebratravelb.booking.Booking;
import cv.terrasystem.zebratravelb.hotel.HotelReservation;
import cv.terrasystem.zebratravelb.order.Order;
import cv.terrasystem.zebratravelb.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Exatamente um de booking/hotelReservation/order fica preenchido por linha — qual dos
// três depende do scope do Voucher (EXCURSION/ROOM/PRODUCT) ou é decidido no momento de
// registar o uso para vouchers de scope ALL.
@Entity
@Table(name = "voucher_redemptions")
@Getter
@Setter
@NoArgsConstructor
public class VoucherRedemption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id", nullable = false)
    private Voucher voucher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_reservation_id")
    private HotelReservation hotelReservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "discount_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @Column(nullable = false)
    private boolean released = false;

    @Column(name = "redeemed_at", nullable = false, updatable = false)
    private LocalDateTime redeemedAt;

    @PrePersist
    void onCreate() {
        redeemedAt = LocalDateTime.now();
    }
}
