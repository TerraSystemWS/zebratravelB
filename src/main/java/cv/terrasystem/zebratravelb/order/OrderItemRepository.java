package cv.terrasystem.zebratravelb.order;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
    boolean existsByProduct_Id(Integer productId);
}
