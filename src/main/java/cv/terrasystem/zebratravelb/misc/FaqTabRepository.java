package cv.terrasystem.zebratravelb.misc;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FaqTabRepository extends JpaRepository<FaqTab, Integer> {
    Optional<FaqTab> findByLabel(String label);
}
