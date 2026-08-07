package cv.terrasystem.zebratravelb.misc;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TravelPackageRepository extends JpaRepository<TravelPackage, Integer> {
    List<TravelPackage> findByImageUrlContaining(String needle);
}
