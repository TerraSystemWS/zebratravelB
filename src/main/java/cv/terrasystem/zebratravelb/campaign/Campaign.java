package cv.terrasystem.zebratravelb.campaign;

import cv.terrasystem.zebratravelb.excursion.Excursion;
import cv.terrasystem.zebratravelb.hotel.HotelRoomType;
import cv.terrasystem.zebratravelb.product.Product;
import cv.terrasystem.zebratravelb.user.User;
import cv.terrasystem.zebratravelb.voucher.Voucher;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

// Um Campaign liga uma imagem (banner) a um dos 4 alvos possíveis — no máximo um dos
// 4 campos voucher/product/excursion/roomType preenchido de cada vez (validado em
// CampaignController, nunca pela BD) — ou nenhum, para um banner 100% manual com
// title/subtitle/linkUrl próprios. Cada FK é ON DELETE SET NULL: apagar o alvo não
// apaga a campanha, só a deixa sem alvo (CampaignService.isLive trata isso como inativa).
@Entity
@Table(name = "campaigns")
@Getter
@Setter
@NoArgsConstructor
public class Campaign {

    public static final String HOME_HERO = "HOME_HERO";
    public static final String HOME_STRIP = "HOME_STRIP";
    public static final String LOJA_TOP = "LOJA_TOP";
    public static final String EXCURSOES_TOP = "EXCURSOES_TOP";
    public static final String HOTEL_TOP = "HOTEL_TOP";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "alt_text")
    private String altText;

    @Column(nullable = false)
    private String placement;

    // EAGER (não LAZY) de propósito: resolveDisplay() precisa sempre do alvo (nome, preço,
    // slug) logo que a campanha é lida, e spring.jpa.open-in-view=false fecha a sessão antes
    // do controller mapear o DTO — o mesmo problema já apanhado e corrigido em
    // VoucherRedemption (ver dev-notes.md secção 19), evitado aqui à partida.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "voucher_id")
    private Voucher voucher;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "excursion_id")
    private Excursion excursion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "room_type_id")
    private HotelRoomType roomType;

    private String title;
    private String subtitle;

    @Column(name = "link_url")
    private String linkUrl;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(nullable = false)
    private Integer priority = 0;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "click_count", nullable = false)
    private Long clickCount = 0L;

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
