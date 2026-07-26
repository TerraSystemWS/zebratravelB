package cv.terrasystem.zebratravelb.product;

import cv.terrasystem.zebratravelb.common.NotFoundException;
import cv.terrasystem.zebratravelb.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    @GetMapping
    public List<CartItemDto> getMyCart(@AuthenticationPrincipal UserPrincipal principal) {
        return cartItemRepository.findByUserId(principal.getId()).stream().map(CartItemDto::from).toList();
    }

    @PostMapping
    public CartItemDto add(@AuthenticationPrincipal UserPrincipal principal, @RequestBody CartItemDto dto) {
        CartItem item = new CartItem();
        item.setUser(principal.getUser());
        if (dto.productId() != null) {
            productRepository.findById(dto.productId()).ifPresent(item::setProduct);
        }
        item.setName(dto.name());
        item.setPrice(dto.price());
        item.setQuantity(dto.quantity() != null ? dto.quantity() : 1);
        item.setImageUrl(dto.imageUrl());
        return CartItemDto.from(cartItemRepository.save(item));
    }

    @PutMapping("/{id}")
    public CartItemDto updateQuantity(@AuthenticationPrincipal UserPrincipal principal,
                                       @PathVariable Integer id,
                                       @RequestBody CartItemDto dto) {
        CartItem item = findOwned(principal.getId(), id);
        item.setQuantity(dto.quantity());
        return CartItemDto.from(cartItemRepository.save(item));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Integer id) {
        findOwned(principal.getId(), id);
        cartItemRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clear(@AuthenticationPrincipal UserPrincipal principal) {
        cartItemRepository.deleteByUserId(principal.getId());
        return ResponseEntity.noContent().build();
    }

    private CartItem findOwned(Integer userId, Integer id) {
        CartItem item = cartItemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Item do carrinho não encontrado: " + id));
        if (!item.getUser().getId().equals(userId)) {
            throw new NotFoundException("Item do carrinho não encontrado: " + id);
        }
        return item;
    }
}
