package cv.terrasystem.zebratravelb.tour;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TourRepository extends JpaRepository<Tour, Integer> {
    List<Tour> findByImageContaining(String needle);

    @Query(value = "SELECT * FROM tours WHERE EXISTS (SELECT 1 FROM unnest(images) img WHERE img LIKE CONCAT('%', :needle, '%'))", nativeQuery = true)
    List<Tour> findByImagesArrayContaining(@Param("needle") String needle);
}
