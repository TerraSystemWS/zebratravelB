package cv.terrasystem.zebratravelb.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
    List<Booking> findByUserId(Integer userId);
    boolean existsByExcursion_SlugAndUser_IdAndStatusIn(String slug, Integer userId, List<String> statuses);
    boolean existsByExcursion_Id(Integer excursionId);
    boolean existsByTour_Id(Integer tourId);
    boolean existsByUserId(Integer userId);

    @Query("""
            select coalesce(sum(b.amount), 0) from Booking b
            where b.status in :statuses and b.createdAt between :start and :end
            """)
    BigDecimal sumRevenue(@Param("statuses") List<String> statuses,
                          @Param("start") LocalDateTime start,
                          @Param("end") LocalDateTime end);
}
