package cv.terrasystem.zebratravelb.post;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuthorRepository extends JpaRepository<Author, Integer> {
    boolean existsByUser_Id(Integer userId);
    List<Author> findByProfileImageContaining(String needle);
}
