package cv.terrasystem.zebratravelb.order;

import java.util.List;

// Venda ao balcão — ADMIN/AGENTE a vender diretamente a um cliente presente, sem conta.
// Paga por Transferência ou Dinheiro (nunca ONLINE — esse fluxo é o checkout do site,
// via Vinti4); a fatura só é emitida quando o pagamento for confirmado (ver
// OrderController.markPaid()), não na criação da venda.
public record CreateCounterSaleRequest(
        List<OrderItemInput> items,
        String paymentMethod,
        String guestName,
        String guestEmail,
        String customerNif,
        String voucherCode
) {
}
