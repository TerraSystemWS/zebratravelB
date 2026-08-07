package cv.terrasystem.zebratravelb.misc;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SponsorRepository extends JpaRepository<Sponsor, Integer> {
    List<Sponsor> findByImageContaining(String needle);
}
