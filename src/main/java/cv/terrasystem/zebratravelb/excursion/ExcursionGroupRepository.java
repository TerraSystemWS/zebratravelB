package cv.terrasystem.zebratravelb.excursion;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExcursionGroupRepository extends JpaRepository<ExcursionGroup, Integer> {
    Optional<ExcursionGroup> findFirstByExcursion_IdAndStatus(Integer excursionId, String status);
    List<ExcursionGroup> findByStatus(String status);
}
