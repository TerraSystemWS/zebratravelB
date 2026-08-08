package cv.terrasystem.zebratravelb.order;

import cv.terrasystem.zebratravelb.common.BadRequestException;
import cv.terrasystem.zebratravelb.common.NotFoundException;
import cv.terrasystem.zebratravelb.product.Product;
import cv.terrasystem.zebratravelb.product.ProductRepository;
import cv.terrasystem.zebratravelb.security.UserPrincipal;
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

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemInput input : request.items()) {
            if (input.quantity() == null || input.quantity() <= 0) {
                throw new BadRequestException("Quantidade inválida para " + input.name());
            }
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setName(input.name());
            item.setPrice(input.price());
            item.setQuantity(input.quantity());
            if (input.productId() != null) {
                Product product = productRepository.findById(input.productId()).orElse(null);
                if (product != null) {
                    if (product.getStockQuantity() < input.quantity()) {
                        throw new BadRequestException("Estoque insuficiente para " + input.name());
                    }
                    product.setStockQuantity(product.getStockQuantity() - input.quantity());
                    productRepository.save(product);
                }
                item.setProduct(product);
            }
            order.getItems().add(item);
            total = total.add(input.price().multiply(BigDecimal.valueOf(input.quantity())));
        }
        order.setTotalAmount(total);

        order.setStatus(switch (order.getPaymentMethod()) {
            case Order.TRANSFER -> Order.AWAITING_TRANSFER;
            case Order.CASH -> Order.AWAITING_CASH;
            default -> Order.PENDING_PAYMENT;
        });

        return OrderDto.from(orderRepository.save(order));
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
