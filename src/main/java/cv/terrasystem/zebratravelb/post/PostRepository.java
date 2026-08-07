package cv.terrasystem.zebratravelb.post;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Integer> {
    Optional<Post> findBySlug(String slug);
    List<Post> findByImageContaining(String needle);
}
