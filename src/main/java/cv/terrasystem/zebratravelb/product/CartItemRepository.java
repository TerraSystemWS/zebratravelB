package cv.terrasystem.zebratravelb.product;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    List<CartItem> findByUserId(Integer userId);
    void deleteByUserId(Integer userId);
    boolean existsByProductId(Integer productId);
    List<CartItem> findByImageUrlContaining(String needle);
}
