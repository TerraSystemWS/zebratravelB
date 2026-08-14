package cv.terrasystem.zebratravelb.voucher;

import cv.terrasystem.zebratravelb.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

// Vouchers (código) e promoções fixas (sem código) partilham esta entidade — a única
// diferença é requiresCode. Regra de negócio (não imposta pela BD, ver VoucherService):
// promoções fixas (requiresCode=false) só podem existir com scope=PRODUCT e scopeItemId
// preenchido — não há promoções automáticas em Excursões/Quartos, só vouchers por código.
@Entity
@Table(name = "vouchers")
@Getter
@Setter
@NoArgsConstructor
public class Voucher {

    public static final String ALL = "ALL";
    public static final String EXCURSION = "EXCURSION";
    public static final String ROOM = "ROOM";
    public static final String PRODUCT = "PRODUCT";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String code;

    @Column(name = "requires_code", nullable = false)
    private boolean requiresCode = true;

    @Column(name = "discount_percent", nullable = false)
    private Integer discountPercent;

    @Column(nullable = false)
    private String scope;

    @Column(name = "scope_item_id")
    private Integer scopeItemId;

    @Column(name = "valid_from")
    private LocalDate validFrom;

    @Column(name = "valid_until")
    private LocalDate validUntil;

    @Column(name = "max_uses")
    private Integer maxUses;

    @Column(name = "max_uses_per_user")
    private Integer maxUsesPerUser = 1;

    @Column(nullable = false)
    private boolean active = true;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
