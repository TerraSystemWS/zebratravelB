package cv.terrasystem.zebratravelb.media;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MediaItemRepository extends JpaRepository<MediaItem, Integer> {
    List<MediaItem> findByFolder_IdIsNull();
    List<MediaItem> findByFolder_Id(Integer folderId);
    Optional<MediaItem> findByStoredFilename(String storedFilename);
}
