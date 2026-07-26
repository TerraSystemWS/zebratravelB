package cv.terrasystem.zebratravelb.order;

import java.util.List;

public record CreateOrderRequest(
        List<OrderItemInput> items,
        String paymentMethod
) {
}
