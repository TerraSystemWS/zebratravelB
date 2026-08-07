package cv.terrasystem.zebratravelb.product;

import cv.terrasystem.zebratravelb.common.ConflictException;
import cv.terrasystem.zebratravelb.common.NotFoundException;
import cv.terrasystem.zebratravelb.common.OwnershipGuard;
import cv.terrasystem.zebratravelb.order.OrderItemRepository;
import cv.terrasystem.zebratravelb.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;
    private final OrderItemRepository orderItemRepository;
    private final FavoriteProductRepository favoriteProductRepository;
    private final CartItemRepository cartItemRepository;

    @GetMapping
    public List<ProductDto> getAll(@RequestParam(defaultValue = "false") boolean includeArchived) {
        return productRepository.findAll().stream()
                .filter(p -> includeArchived || !"ARCHIVED".equals(p.getStatus()))
                .map(ProductDto::from)
                .toList();
    }

    @GetMapping("/{id}")
    public ProductDto getById(@PathVariable Integer id) {
        return ProductDto.from(find(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public ProductDto create(@AuthenticationPrincipal UserPrincipal principal, @RequestBody ProductDto dto) {
        Product product = new Product();
        applyTo(product, dto);
        product.setCreatedBy(principal.getUser());
        return ProductDto.from(productRepository.save(product));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public ProductDto update(@PathVariable Integer id, @RequestBody ProductDto dto) {
        Product product = find(id);
        applyTo(product, dto);
        return ProductDto.from(productRepository.save(product));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Integer id) {
        Product product = find(id);
        OwnershipGuard.requireOwnerOrAdmin(principal, product.getCreatedBy());
        if (orderItemRepository.existsByProduct_Id(product.getId())
                || favoriteProductRepository.existsByProductId(product.getId())
                || cartItemRepository.existsByProductId(product.getId())) {
            throw new ConflictException("Não é possível apagar: existem encomendas, favoritos ou carrinhos com este produto. Arquive em vez de apagar.");
        }
        productRepository.delete(product);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/archive")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public ProductDto archive(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Integer id) {
        Product product = find(id);
        OwnershipGuard.requireOwnerOrAdmin(principal, product.getCreatedBy());
        product.setStatus("ARCHIVED");
        return ProductDto.from(productRepository.save(product));
    }

    @PostMapping("/{id}/restore")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public ProductDto restore(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Integer id) {
        Product product = find(id);
        OwnershipGuard.requireOwnerOrAdmin(principal, product.getCreatedBy());
        product.setStatus("ACTIVE");
        return ProductDto.from(productRepository.save(product));
    }

    private Product find(Integer id) {
        return productRepository.findById(id).orElseThrow(() -> new NotFoundException("Produto não encontrado: " + id));
    }

    private void applyTo(Product product, ProductDto dto) {
        product.setTitle(dto.title());
        product.setPrice(dto.price());
        product.setImageUrl(dto.imageUrl());
        product.setLink(dto.link());
        product.setStockQuantity(dto.stockQuantity() != null ? dto.stockQuantity() : 0);
        if (dto.category() != null) {
            ProductCategory category = categoryRepository.findByName(dto.category())
                    .orElseGet(() -> {
                        ProductCategory c = new ProductCategory();
                        c.setName(dto.category());
                        return categoryRepository.save(c);
                    });
            product.setCategory(category);
        }
    }
}
