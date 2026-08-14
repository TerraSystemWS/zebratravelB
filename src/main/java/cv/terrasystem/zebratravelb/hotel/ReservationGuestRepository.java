package cv.terrasystem.zebratravelb.hotel;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationGuestRepository extends JpaRepository<ReservationGuest, Integer> {

    List<ReservationGuest> findByReservation_IdOrderByCreatedAtAsc(Integer reservationId);

    boolean existsByIdAndReservation_Id(Integer id, Integer reservationId);
}
