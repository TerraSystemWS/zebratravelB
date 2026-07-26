package cv.terrasystem.zebratravelb.booking;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
    List<Booking> findByUserId(Integer userId);
    boolean existsByExcursion_SlugAndUser_IdAndStatusIn(String slug, Integer userId, List<String> statuses);
}
