package cv.terrasystem.zebratravelb.order;

import cv.terrasystem.zebratravelb.common.BadRequestException;
import cv.terrasystem.zebratravelb.common.NotFoundException;
import cv.terrasystem.zebratravelb.product.Product;
import cv.terrasystem.zebratravelb.product.ProductRepository;
import cv.terrasystem.zebratravelb.security.UserPrincipal;
import cv.terrasystem.zebratravelb.voucher.Voucher;
import cv.terrasystem.zebratravelb.voucher.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private static final Set<String> PAYMENT_METHODS = Set.of(Order.ONLINE, Order.TRANSFER, Order.CASH);
    private static final Set<String> FULFILLMENT_STATUSES = Set.of(Order.PENDING_SHIPMENT, Order.SHIPPED, Order.DELIVERED);

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final VoucherService voucherService;

    @GetMapping("/mine")
    public List<OrderDto> getMine(@AuthenticationPrincipal UserPrincipal principal) {
        return orderRepository.findByUser_IdOrderByCreatedAtDesc(principal.getId()).stream()
                .map(OrderDto::from)
                .toList();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public List<OrderDto> getAll() {
        return orderRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(OrderDto::from)
                .toList();
    }

    @PatchMapping("/{id}/fulfillment-status")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public OrderDto updateFulfillmentStatus(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Encomenda não encontrada: " + id));
        String fulfillmentStatus = body.get("fulfillmentStatus");
        if (fulfillmentStatus == null || !FULFILLMENT_STATUSES.contains(fulfillmentStatus.toUpperCase())) {
            throw new BadRequestException("fulfillmentStatus inválido: " + fulfillmentStatus);
        }
        // Não exigimos status == PAID: encomendas TRANSFER/CASH não têm hoje nenhum
        // endpoint para serem marcadas como pagas manualmente (limitação conhecida,
        // ver dev-notes.md secção 4) — bloquear aqui deixaria o envio delas impossível
        // de sempre. O admin decide quando faz sentido tratar o envio.
        order.setFulfillmentStatus(fulfillmentStatus.toUpperCase());
        return OrderDto.from(orderRepository.save(order));
    }

    @GetMapping("/{id}")
    public OrderDto getOne(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Integer id) {
        Order order = findOwned(principal.getId(), id);
        return OrderDto.from(order);
    }

    @PostMapping
    public OrderDto create(@AuthenticationPrincipal UserPrincipal principal, @RequestBody CreateOrderRequest request) {
        if (request.items() == null || request.items().isEmpty()) {
            throw new BadRequestException("O carrinho está vazio");
        }
        if (request.paymentMethod() == null || !PAYMENT_METHODS.contains(request.paymentMethod().toUpperCase())) {
            throw new BadRequestException("Método de pagamento inválido");
        }

        Order order = new Order();
        order.setUser(principal.getUser());
        order.setPaymentMethod(request.paymentMethod().toUpperCase());

        // Um voucher só, válido para a encomenda inteira — a decisão de a que linha se aplica
        // (âmbito ALL/PRODUCT genérico ou um produto específico) é feita item a item abaixo,
        // por isso a verificação aqui passa itemId=null (ver VoucherService.checkApplicable).
        Voucher voucher = null;
        if (request.voucherCode() != null && !request.voucherCode().isBlank()) {
            voucher = voucherService.validateCode(request.voucherCode(), Voucher.PRODUCT, null, principal.getUser());
        }

        BigDecimal total = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        for (OrderItemInput input : request.items()) {
            if (input.quantity() == null || input.quantity() <= 0) {
                throw new BadRequestException("Quantidade inválida para " + input.name());
            }
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setName(input.name());
            item.setQuantity(input.quantity());

            // O preço nunca vem do cliente para itens com productId — é sempre recalculado a
            // partir do Product (com a promoção fixa ativa, se houver), para o desconto (e a
            // regra de não acumular com voucher) serem garantidos pelo servidor, não confiados
            // ao pedido. Sem productId (caso raro/legado), mantém-se o preço enviado.
            BigDecimal unitPrice = input.price();
            if (input.productId() != null) {
                Product product = productRepository.findById(input.productId()).orElse(null);
                if (product != null) {
                    if (product.getStockQuantity() < input.quantity()) {
                        throw new BadRequestException("Estoque insuficiente para " + input.name());
                    }
                    product.setStockQuantity(product.getStockQuantity() - input.quantity());
                    productRepository.save(product);

                    Voucher promotion = voucherService.findActivePromotion(product.getId()).orElse(null);
                    if (promotion != null) {
                        // Produto já com promoção fixa: não acumula com voucher, mesmo que o
                        // voucher se aplicasse a este produto — decisão confirmada com o utilizador.
                        unitPrice = voucherService.applyDiscount(promotion, product.getPrice());
                    } else {
                        unitPrice = product.getPrice();
                        if (voucher != null && voucherService.appliesToItem(voucher, Voucher.PRODUCT, product.getId())) {
                            BigDecimal discounted = voucherService.applyDiscount(voucher, unitPrice);
                            totalDiscount = totalDiscount.add(unitPrice.subtract(discounted).multiply(BigDecimal.valueOf(input.quantity())));
                            unitPrice = discounted;
                        }
                    }
                }
                item.setProduct(product);
            }
            item.setPrice(unitPrice);
            order.getItems().add(item);
            total = total.add(unitPrice.multiply(BigDecimal.valueOf(input.quantity())));
        }

        if (voucher != null && totalDiscount.signum() == 0) {
            throw new BadRequestException("Este voucher não se aplica a nenhum produto deste carrinho");
        }
        order.setTotalAmount(total);

        order.setStatus(switch (order.getPaymentMethod()) {
            case Order.TRANSFER -> Order.AWAITING_TRANSFER;
            case Order.CASH -> Order.AWAITING_CASH;
            default -> Order.PENDING_PAYMENT;
        });

        Order saved = orderRepository.save(order);
        if (voucher != null) {
            voucherService.recordRedemption(voucher, principal.getUser(), totalDiscount, null, null, saved);
        }
        return OrderDto.from(saved);
    }

    private Order findOwned(Integer userId, Integer id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Encomenda não encontrada: " + id));
        if (order.getUser() == null || !order.getUser().getId().equals(userId)) {
            throw new NotFoundException("Encomenda não encontrada: " + id);
        }
        return order;
    }
}
