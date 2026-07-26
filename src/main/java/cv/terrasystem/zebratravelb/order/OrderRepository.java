package cv.terrasystem.zebratravelb.order;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByUser_IdOrderByCreatedAtDesc(Integer userId);
    Optional<Order> findByMerchantRef(String merchantRef);
}
