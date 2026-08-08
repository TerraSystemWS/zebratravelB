package cv.terrasystem.zebratravelb.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDto(
        Integer id,
        String userName,
        String userEmail,
        BigDecimal totalAmount,
        String paymentMethod,
        String status,
        String fulfillmentStatus,
        LocalDateTime createdAt,
        List<OrderItemDto> items
) {
    public static OrderDto from(Order order) {
        return new OrderDto(
                order.getId(),
                order.getUser() != null ? (order.getUser().getFullName() != null ? order.getUser().getFullName() : order.getUser().getUsername()) : "N/A",
                order.getUser() != null ? order.getUser().getEmail() : null,
                order.getTotalAmount(),
                order.getPaymentMethod(),
                order.getStatus(),
                order.getFulfillmentStatus(),
                order.getCreatedAt(),
                order.getItems().stream().map(OrderItemDto::from).toList()
        );
    }
}
