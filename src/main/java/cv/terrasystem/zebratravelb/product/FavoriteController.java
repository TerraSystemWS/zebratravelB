package cv.terrasystem.zebratravelb.product;

import cv.terrasystem.zebratravelb.common.NotFoundException;
import cv.terrasystem.zebratravelb.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteProductRepository favoriteRepository;
    private final ProductRepository productRepository;

    @GetMapping
    public List<ProductDto> getMyFavorites(@AuthenticationPrincipal UserPrincipal principal) {
        return favoriteRepository.findByUserId(principal.getId()).stream()
                .map(f -> ProductDto.from(f.getProduct()))
                .toList();
    }

    @PostMapping
    public ResponseEntity<Void> add(@AuthenticationPrincipal UserPrincipal principal, @RequestBody Map<String, Integer> body) {
        Integer productId = body.get("productId");
        if (favoriteRepository.findByUserIdAndProductId(principal.getId(), productId).isPresent()) {
            return ResponseEntity.ok().build();
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Produto não encontrado: " + productId));
        FavoriteProduct favorite = new FavoriteProduct();
        favorite.setUser(principal.getUser());
        favorite.setProduct(product);
        favoriteRepository.save(favorite);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> remove(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Integer productId) {
        favoriteRepository.deleteByUserIdAndProductId(principal.getId(), productId);
        return ResponseEntity.noContent().build();
    }
}
