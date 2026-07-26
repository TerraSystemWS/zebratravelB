package cv.terrasystem.zebratravelb.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDto(
        Integer id,
        BigDecimal totalAmount,
        String paymentMethod,
        String status,
        LocalDateTime createdAt,
        List<OrderItemDto> items
) {
    public static OrderDto from(Order order) {
        return new OrderDto(
                order.getId(),
                order.getTotalAmount(),
                order.getPaymentMethod(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getItems().stream().map(OrderItemDto::from).toList()
        );
    }
}
