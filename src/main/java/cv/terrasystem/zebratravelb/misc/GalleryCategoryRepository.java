package cv.terrasystem.zebratravelb.misc;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GalleryCategoryRepository extends JpaRepository<GalleryCategory, Integer> {
    Optional<GalleryCategory> findByName(String name);
}
