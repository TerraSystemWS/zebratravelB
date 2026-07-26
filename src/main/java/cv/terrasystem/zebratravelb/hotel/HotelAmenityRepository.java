package cv.terrasystem.zebratravelb.hotel;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HotelAmenityRepository extends JpaRepository<HotelAmenity, Integer> {
    Optional<HotelAmenity> findByCodeIgnoreCase(String code);
}
