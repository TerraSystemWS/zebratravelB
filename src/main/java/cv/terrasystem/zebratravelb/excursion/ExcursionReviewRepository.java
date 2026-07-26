package cv.terrasystem.zebratravelb.excursion;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExcursionReviewRepository extends JpaRepository<ExcursionReview, Integer> {
    List<ExcursionReview> findByExcursion_SlugOrderByCreatedAtDesc(String slug);
    boolean existsByExcursion_SlugAndUser_Id(String slug, Integer userId);
}
