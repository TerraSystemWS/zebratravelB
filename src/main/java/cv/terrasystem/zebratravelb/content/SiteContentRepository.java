package cv.terrasystem.zebratravelb.content;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SiteContentRepository extends JpaRepository<SiteContent, String> {

    @Query(value = "SELECT * FROM site_content WHERE content_value::text LIKE CONCAT('%', :needle, '%')", nativeQuery = true)
    List<SiteContent> findByContentValueContaining(@Param("needle") String needle);
}
