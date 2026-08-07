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
        String status
) {
    public static ProductDto from(Product p) {
        return new ProductDto(
                p.getId(), p.getTitle(), p.getPrice(), p.getImageUrl(), p.getLink(),
                p.getCategory() != null ? p.getCategory().getName() : null,
                p.getStockQuantity(),
                p.getCreatedBy() != null ? p.getCreatedBy().getId() : null,
                p.getStatus()
        );
    }
}
