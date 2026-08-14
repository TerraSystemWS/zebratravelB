package cv.terrasystem.zebratravelb.order;

import cv.terrasystem.zebratravelb.common.BadRequestException;
import cv.terrasystem.zebratravelb.common.NotFoundException;
import cv.terrasystem.zebratravelb.invoice.InvoiceService;
import cv.terrasystem.zebratravelb.product.Product;
import cv.terrasystem.zebratravelb.product.ProductRepository;
import cv.terrasystem.zebratravelb.security.UserPrincipal;
import cv.terrasystem.zebratravelb.voucher.Voucher;
import cv.terrasystem.zebratravelb.voucher.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private static final Set<String> PAYMENT_METHODS = Set.of(Order.ONLINE, Order.TRANSFER, Order.CASH);
    private static final Set<String> COUNTER_SALE_PAYMENT_METHODS = Set.of(Order.TRANSFER, Order.CASH);
    private static final Set<String> FULFILLMENT_STATUSES = Set.of(Order.PENDING_SHIPMENT, Order.SHIPPED, Order.DELIVERED);
    private static final Set<String> AWAITING_MANUAL_PAYMENT = Set.of(Order.AWAITING_TRANSFER, Order.AWAITING_CASH);

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final VoucherService voucherService;
    private final InvoiceService invoiceService;

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
        // Não exigimos status == PAID: nem toda encomenda TRANSFER/CASH está paga no momento
        // do envio (ver markPaid() para o fluxo normal) — o admin decide quando faz sentido
        // tratar o envio, mesmo que ainda não tenha marcado o pagamento.
        order.setFulfillmentStatus(fulfillmentStatus.toUpperCase());
        return OrderDto.from(orderRepository.save(order));
    }

    // Fecha a lacuna já conhecida (ver tarefas.md/dev-notes.md): encomendas por Transferência
    // ou Dinheiro não tinham nenhuma forma de serem marcadas como pagas — nem as feitas pelo
    // cliente no site, nem as de venda ao balcão. Emite a fatura no mesmo passo (issueForOrder
    // é idempotente, nunca duplica se já existir uma para esta encomenda).
    // @Transactional aqui é importante: se a emissão da fatura falhar depois de gravar o
    // estado PAID, sem isto a encomenda ficava presa em PAID sem fatura nenhuma e sem
    // conseguir tentar de novo (mark-paid só aceita AWAITING_CASH/AWAITING_TRANSFER).
    @PatchMapping("/{id}/mark-paid")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    @Transactional
    public OrderDto markPaid(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Integer id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Encomenda não encontrada: " + id));
        if (!AWAITING_MANUAL_PAYMENT.contains(order.getStatus())) {
            throw new BadRequestException("Só é possível marcar como paga uma encomenda à espera de transferência ou dinheiro");
        }
        order.setStatus(Order.PAID);
        Order saved = orderRepository.save(order);
        invoiceService.issueForOrder(saved, principal.getUser());
        return OrderDto.from(saved);
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
        order.setCustomerNif(request.customerNif());

        // Um voucher só, válido para a encomenda inteira — a decisão de a que linha se aplica
        // (âmbito ALL/PRODUCT genérico ou um produto específico) é feita item a item abaixo,
        // por isso a verificação aqui passa itemId=null (ver VoucherService.checkApplicable).
        Voucher voucher = null;
        if (request.voucherCode() != null && !request.voucherCode().isBlank()) {
            voucher = voucherService.validateCode(request.voucherCode(), Voucher.PRODUCT, null, principal.getUser());
        }

        ItemsResult result = buildItems(order, request.items(), voucher);
        if (voucher != null && result.totalDiscount().signum() == 0) {
            throw new BadRequestException("Este voucher não se aplica a nenhum produto deste carrinho");
        }
        order.getItems().addAll(result.items());
        order.setTotalAmount(result.total());

        order.setStatus(switch (order.getPaymentMethod()) {
            case Order.TRANSFER -> Order.AWAITING_TRANSFER;
            case Order.CASH -> Order.AWAITING_CASH;
            default -> Order.PENDING_PAYMENT;
        });

        Order saved = orderRepository.save(order);
        if (voucher != null) {
            voucherService.recordRedemption(voucher, principal.getUser(), result.totalDiscount(), null, null, saved);
        }
        return OrderDto.from(saved);
    }

    // Venda ao balcão: ADMIN/AGENTE cria a encomenda em nome de um cliente presente, sem
    // conta. Fica em AWAITING_TRANSFER/AWAITING_CASH tal como uma encomenda normal por esses
    // métodos — a fatura só é emitida quando markPaid() confirmar o pagamento, não aqui.
    @PostMapping("/counter-sale")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public OrderDto counterSale(@AuthenticationPrincipal UserPrincipal principal, @RequestBody CreateCounterSaleRequest request) {
        if (request.items() == null || request.items().isEmpty()) {
            throw new BadRequestException("O carrinho está vazio");
        }
        if (request.paymentMethod() == null || !COUNTER_SALE_PAYMENT_METHODS.contains(request.paymentMethod().toUpperCase())) {
            throw new BadRequestException("Método de pagamento inválido para venda ao balcão");
        }
        if (request.guestName() == null || request.guestName().isBlank()) {
            throw new BadRequestException("Nome do cliente é obrigatório");
        }

        Order order = new Order();
        order.setGuestName(request.guestName());
        order.setGuestEmail(request.guestEmail());
        order.setCustomerNif(request.customerNif());
        order.setPaymentMethod(request.paymentMethod().toUpperCase());

        Voucher voucher = null;
        if (request.voucherCode() != null && !request.voucherCode().isBlank()) {
            voucher = voucherService.validateCode(request.voucherCode(), Voucher.PRODUCT, null, principal.getUser());
        }

        ItemsResult result = buildItems(order, request.items(), voucher);
        if (voucher != null && result.totalDiscount().signum() == 0) {
            throw new BadRequestException("Este voucher não se aplica a nenhum produto deste carrinho");
        }
        order.getItems().addAll(result.items());
        order.setTotalAmount(result.total());
        order.setStatus(Order.TRANSFER.equals(order.getPaymentMethod()) ? Order.AWAITING_TRANSFER : Order.AWAITING_CASH);

        Order saved = orderRepository.save(order);
        if (voucher != null) {
            voucherService.recordRedemption(voucher, principal.getUser(), result.totalDiscount(), null, null, saved);
        }
        return OrderDto.from(saved);
    }

    private record ItemsResult(List<OrderItem> items, BigDecimal total, BigDecimal totalDiscount) {
    }

    // Partilhado por create() e counterSale() — preço nunca vem do cliente para itens com
    // productId (recalculado a partir do Product, com a promoção fixa se houver), stock
    // decrementado, voucher aplicado item a item quando não há promoção já ativa.
    private ItemsResult buildItems(Order order, List<OrderItemInput> inputs, Voucher voucher) {
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();

        for (OrderItemInput input : inputs) {
            if (input.quantity() == null || input.quantity() <= 0) {
                throw new BadRequestException("Quantidade inválida para " + input.name());
            }
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setName(input.name());
            item.setQuantity(input.quantity());

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
            items.add(item);
            total = total.add(unitPrice.multiply(BigDecimal.valueOf(input.quantity())));
        }

        return new ItemsResult(items, total, totalDiscount);
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
