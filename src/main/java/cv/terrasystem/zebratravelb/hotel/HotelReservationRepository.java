package cv.terrasystem.zebratravelb.hotel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HotelReservationRepository extends JpaRepository<HotelReservation, Integer> {

    List<HotelReservation> findByUser_IdOrderByCreatedAtDesc(Integer userId);

    List<HotelReservation> findByHotel_IdAndCheckInLessThanAndCheckOutGreaterThan(
            Integer hotelId, LocalDate before, LocalDate after);

    @Query("""
            SELECT r FROM HotelReservation r
            WHERE r.room.id = :roomId
              AND r.status IN ('PENDING_PAYMENT', 'AWAITING_TRANSFER', 'AWAITING_CASH', 'ON_HOLD', 'CONFIRMED', 'PAID')
              AND r.checkIn < :checkOut
              AND r.checkOut > :checkIn
            """)
    List<HotelReservation> findOverlapping(@Param("roomId") Integer roomId,
                                            @Param("checkIn") LocalDate checkIn,
                                            @Param("checkOut") LocalDate checkOut);

    Optional<HotelReservation> findByMerchantRef(String merchantRef);

    boolean existsByRoom_IdAndUser_IdAndStatusIn(Integer roomId, Integer userId, List<String> statuses);
}
