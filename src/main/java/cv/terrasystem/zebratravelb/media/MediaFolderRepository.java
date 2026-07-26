package cv.terrasystem.zebratravelb.media;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MediaFolderRepository extends JpaRepository<MediaFolder, Integer> {
    List<MediaFolder> findByParent_IdIsNull();
    List<MediaFolder> findByParent_Id(Integer parentId);
    boolean existsByParent_IdAndName(Integer parentId, String name);
    boolean existsByParent_IdIsNullAndName(String name);
}
