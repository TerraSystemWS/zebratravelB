package cv.terrasystem.zebratravelb.excursion;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExcursionRepository extends JpaRepository<Excursion, Integer> {
    Optional<Excursion> findBySlug(String slug);
    void deleteBySlug(String slug);
    boolean existsBySlug(String slug);
}
