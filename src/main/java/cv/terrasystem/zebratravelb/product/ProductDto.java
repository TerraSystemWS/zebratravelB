package cv.terrasystem.zebratravelb.product;

import java.math.BigDecimal;

public record ProductDto(
        Integer id,
        String title,
        BigDecimal price,
        String imageUrl,
        String link,
        String category,
        Integer stockQuantity,
        Integer createdById,
        String status,
        // Preenchidos só quando há uma promoção fixa ativa para este produto (ver
        // VoucherService.findActivePromotion) — null nos restantes casos. Só Produtos da
        // Loja têm promoções automáticas, decisão confirmada com o utilizador.
        Integer discountPercent,
        BigDecimal promoPrice
) {
    public static ProductDto from(Product p) {
        return from(p, null, null);
    }

    public static ProductDto from(Product p, Integer discountPercent, BigDecimal promoPrice) {
        return new ProductDto(
                p.getId(), p.getTitle(), p.getPrice(), p.getImageUrl(), p.getLink(),
                p.getCategory() != null ? p.getCategory().getName() : null,
                p.getStockQuantity(),
                p.getCreatedBy() != null ? p.getCreatedBy().getId() : null,
                p.getStatus(),
                discountPercent, promoPrice
        );
    }
}
