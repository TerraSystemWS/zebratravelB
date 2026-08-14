package cv.terrasystem.zebratravelb.campaign;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CampaignDto(
        Integer id,
        String name,
        String imageUrl,
        String altText,
        String placement,
        Integer voucherId,
        Integer productId,
        Integer excursionId,
        Integer roomTypeId,
        String targetLabel,
        String title,
        String subtitle,
        String linkUrl,
        LocalDate startDate,
        LocalDate endDate,
        Integer priority,
        boolean active,
        Long clickCount,
        Integer createdById,
        LocalDateTime createdAt,
        String status
) {
    public static CampaignDto from(Campaign c, CampaignService.CampaignDisplay display, boolean live) {
        String targetLabel = null;
        if (c.getVoucher() != null) {
            targetLabel = "Voucher " + (c.getVoucher().getCode() != null ? c.getVoucher().getCode() : "(promoção)");
        } else if (c.getProduct() != null) {
            targetLabel = "Produto: " + c.getProduct().getTitle();
        } else if (c.getExcursion() != null) {
            targetLabel = "Excursão: " + c.getExcursion().getTitle();
        } else if (c.getRoomType() != null) {
            targetLabel = "Quarto: " + c.getRoomType().getHotel().getName() + " — " + c.getRoomType().getName();
        }

        String status;
        if (!c.isActive()) {
            status = "Inativa";
        } else if (c.getStartDate() != null && LocalDate.now().isBefore(c.getStartDate())) {
            status = "Agendada";
        } else if (c.getEndDate() != null && LocalDate.now().isAfter(c.getEndDate())) {
            status = "Expirada";
        } else if (!live) {
            status = "Alvo indisponível";
        } else {
            status = "Ativa";
        }

        return new CampaignDto(
                c.getId(), c.getName(), c.getImageUrl(), c.getAltText(), c.getPlacement(),
                c.getVoucher() != null ? c.getVoucher().getId() : null,
                c.getProduct() != null ? c.getProduct().getId() : null,
                c.getExcursion() != null ? c.getExcursion().getId() : null,
                c.getRoomType() != null ? c.getRoomType().getId() : null,
                targetLabel,
                display.title(), display.subtitle(), display.linkUrl(),
                c.getStartDate(), c.getEndDate(), c.getPriority(), c.isActive(), c.getClickCount(),
                c.getCreatedBy() != null ? c.getCreatedBy().getId() : null,
                c.getCreatedAt(), status
        );
    }
}
