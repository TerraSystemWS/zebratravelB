package cv.terrasystem.zebratravelb.order;

import java.math.BigDecimal;

public record OrderItemInput(
        Integer productId,
        String name,
        BigDecimal price,
        Integer quantity
) {
}
