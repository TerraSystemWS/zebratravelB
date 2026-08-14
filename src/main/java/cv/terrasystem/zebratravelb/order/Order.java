package cv.terrasystem.zebratravelb.order;

import cv.terrasystem.zebratravelb.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class Order {

    public static final String ONLINE = "ONLINE";
    public static final String TRANSFER = "TRANSFER";
    public static final String CASH = "CASH";

    public static final String PENDING_PAYMENT = "PENDING_PAYMENT";
    public static final String AWAITING_TRANSFER = "AWAITING_TRANSFER";
    public static final String AWAITING_CASH = "AWAITING_CASH";
    public static final String PAID = "PAID";
    public static final String FAILED = "FAILED";
    public static final String CANCELLED = "CANCELLED";

    public static final String PENDING_SHIPMENT = "PENDING_SHIPMENT";
    public static final String SHIPPED = "SHIPPED";
    public static final String DELIVERED = "DELIVERED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    // Preenchidos só numa venda ao balcão (ADMIN/AGENTE a vender a um cliente sem conta) —
    // mesmo padrão de HotelReservation.guestName/Booking.guestName.
    @Column(name = "guest_name")
    private String guestName;
    @Column(name = "guest_email")
    private String guestEmail;

    // Opcional, para qualquer encomenda (com ou sem conta) — sem ele, a fatura sai como
    // "Consumidor Final" (ver invoice/InvoiceService).
    @Column(name = "customer_nif")
    private String customerNif;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod;

    @Column(nullable = false)
    private String status = PENDING_PAYMENT;

    // Só faz sentido depois de paga — tratamento de shipping/entrega da encomenda,
    // separado do estado de pagamento (ver dashboard/loja/encomendas no zebraDM).
    @Column(name = "fulfillment_status", nullable = false)
    private String fulfillmentStatus = PENDING_SHIPMENT;

    @Column(name = "merchant_ref")
    private String merchantRef;

    @Column(name = "merchant_session")
    private String merchantSession;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
