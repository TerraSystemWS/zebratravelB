package cv.terrasystem.zebratravelb.favorite;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Integer> {
    List<Favorite> findByUserId(Integer userId);
    Optional<Favorite> findByUserIdAndItemTypeAndItemId(Integer userId, String itemType, Integer itemId);
    boolean existsByItemTypeAndItemId(String itemType, Integer itemId);
}
