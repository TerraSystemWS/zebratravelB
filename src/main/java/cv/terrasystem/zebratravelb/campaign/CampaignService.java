package cv.terrasystem.zebratravelb.campaign;

import cv.terrasystem.zebratravelb.excursion.Excursion;
import cv.terrasystem.zebratravelb.hotel.HotelRoomType;
import cv.terrasystem.zebratravelb.product.Product;
import cv.terrasystem.zebratravelb.voucher.Voucher;
import cv.terrasystem.zebratravelb.voucher.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CampaignService {

    private final VoucherService voucherService;

    // "Ao vivo" = ativa + dentro da janela de datas + (se ligada a um alvo) esse alvo
    // continua válido — apagar/arquivar o alvo não apaga a campanha (FK ON DELETE SET
    // NULL), só a desliga automaticamente em vez de continuar a anunciar algo que já não existe.
    public boolean isLive(Campaign c) {
        if (!c.isActive()) {
            return false;
        }
        LocalDate today = LocalDate.now();
        if (c.getStartDate() != null && today.isBefore(c.getStartDate())) {
            return false;
        }
        if (c.getEndDate() != null && today.isAfter(c.getEndDate())) {
            return false;
        }
        if (c.getVoucher() != null && !c.getVoucher().isActive()) {
            return false;
        }
        if (c.getProduct() != null && !"ACTIVE".equals(c.getProduct().getStatus())) {
            return false;
        }
        if (c.getExcursion() != null && !"ACTIVE".equals(c.getExcursion().getStatus())) {
            return false;
        }
        if (c.getRoomType() != null && !"ACTIVE".equals(c.getRoomType().getStatus())) {
            return false;
        }
        return true;
    }

    // Título/subtítulo/link são derivados do alvo ligado (voucher/produto/excursão/quarto)
    // quando a campanha não tem os seus próprios (title/subtitle/linkUrl servem de
    // sobreposição manual) — no máximo um alvo está preenchido de cada vez, ver Campaign.java.
    public CampaignDisplay resolveDisplay(Campaign c) {
        if (c.getVoucher() != null) {
            Voucher v = c.getVoucher();
            String ribbon = "-" + v.getDiscountPercent() + "%";
            String subtitle = v.isRequiresCode() ? "Código " + v.getCode() : ribbon;
            return new CampaignDisplay(
                    c.getTitle() != null ? c.getTitle() : defaultTitleForVoucherScope(v.getScope()),
                    c.getSubtitle() != null ? c.getSubtitle() : subtitle,
                    c.getLinkUrl() != null ? c.getLinkUrl() : defaultLinkForVoucherScope(v.getScope()),
                    ribbon
            );
        }
        if (c.getProduct() != null) {
            Product p = c.getProduct();
            Voucher promo = voucherService.findActivePromotion(p.getId()).orElse(null);
            return new CampaignDisplay(
                    c.getTitle() != null ? c.getTitle() : p.getTitle(),
                    c.getSubtitle(),
                    c.getLinkUrl() != null ? c.getLinkUrl() : "/loja",
                    promo != null ? "-" + promo.getDiscountPercent() + "%" : null
            );
        }
        if (c.getExcursion() != null) {
            Excursion e = c.getExcursion();
            return new CampaignDisplay(
                    c.getTitle() != null ? c.getTitle() : e.getTitle(),
                    c.getSubtitle() != null ? c.getSubtitle() : e.getLocation(),
                    c.getLinkUrl() != null ? c.getLinkUrl() : "/excurcoes/" + e.getSlug(),
                    null
            );
        }
        if (c.getRoomType() != null) {
            HotelRoomType rt = c.getRoomType();
            return new CampaignDisplay(
                    c.getTitle() != null ? c.getTitle() : rt.getHotel().getName() + " — " + rt.getName(),
                    c.getSubtitle(),
                    c.getLinkUrl() != null ? c.getLinkUrl() : "/hotel/" + rt.getHotel().getId(),
                    null
            );
        }
        return new CampaignDisplay(c.getTitle(), c.getSubtitle(), c.getLinkUrl(), null);
    }

    private String defaultTitleForVoucherScope(String scope) {
        return switch (scope) {
            case Voucher.EXCURSION -> "Desconto em Excursões";
            case Voucher.ROOM -> "Desconto em Quartos";
            case Voucher.PRODUCT -> "Desconto na Loja";
            default -> "Oferta especial";
        };
    }

    private String defaultLinkForVoucherScope(String scope) {
        return switch (scope) {
            case Voucher.EXCURSION -> "/excurcoes";
            case Voucher.ROOM -> "/hotel";
            case Voucher.PRODUCT -> "/loja";
            default -> "/";
        };
    }

    public record CampaignDisplay(String title, String subtitle, String linkUrl, String ribbon) {
    }
}
