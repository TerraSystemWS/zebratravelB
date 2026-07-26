package cv.terrasystem.zebratravelb.post;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostCategoryRepository extends JpaRepository<PostCategory, Integer> {
    Optional<PostCategory> findByName(String name);
}
