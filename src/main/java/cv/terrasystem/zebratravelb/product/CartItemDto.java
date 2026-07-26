package cv.terrasystem.zebratravelb.product;

import java.math.BigDecimal;

public record CartItemDto(
        Integer id,
        Integer productId,
        String name,
        BigDecimal price,
        Integer quantity,
        String imageUrl
) {
    public static CartItemDto from(CartItem item) {
        return new CartItemDto(
                item.getId(),
                item.getProduct() != null ? item.getProduct().getId() : null,
                item.getName(), item.getPrice(), item.getQuantity(), item.getImageUrl()
        );
    }
}
