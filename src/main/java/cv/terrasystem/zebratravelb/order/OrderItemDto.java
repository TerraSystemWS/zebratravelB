package cv.terrasystem.zebratravelb.order;

import java.math.BigDecimal;

public record OrderItemDto(String name, BigDecimal price, Integer quantity) {
    public static OrderItemDto from(OrderItem item) {
        return new OrderItemDto(item.getName(), item.getPrice(), item.getQuantity());
    }
}
