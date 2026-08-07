package cv.terrasystem.zebratravelb.excursion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ExcursionReviewRepository extends JpaRepository<ExcursionReview, Integer> {
    List<ExcursionReview> findByExcursion_SlugOrderByCreatedAtDesc(String slug);
    boolean existsByExcursion_SlugAndUser_Id(String slug, Integer userId);
    long countByExcursion_Slug(String slug);
    boolean existsByExcursion_Id(Integer excursionId);
    boolean existsByUser_Id(Integer userId);

    @Query("select avg(r.rating) from ExcursionReview r where r.excursion.slug = :slug")
    Optional<Double> findAverageRatingByExcursion_Slug(String slug);

    @Query("select coalesce(sum(r.rating), 0) from ExcursionReview r")
    long sumRating();
}
