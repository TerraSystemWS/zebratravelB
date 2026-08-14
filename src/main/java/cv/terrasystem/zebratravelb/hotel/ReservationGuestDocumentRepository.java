package cv.terrasystem.zebratravelb.hotel;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationGuestDocumentRepository extends JpaRepository<ReservationGuestDocument, Integer> {

    List<ReservationGuestDocument> findByGuest_IdOrderByUploadedAtAsc(Integer guestId);

    boolean existsByIdAndGuest_Id(Integer id, Integer guestId);
}
