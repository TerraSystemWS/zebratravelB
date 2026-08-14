package cv.terrasystem.zebratravelb.order;

import java.util.List;

public record CreateOrderRequest(
        List<OrderItemInput> items,
        String paymentMethod,
        String voucherCode   // opcional — aplicado ao total, ignorado em linhas com promoção fixa ativa
) {
}
