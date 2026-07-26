package cv.terrasystem.zebratravelb.hotel;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HotelRoomReviewRepository extends JpaRepository<HotelRoomReview, Integer> {
    List<HotelRoomReview> findByRoom_IdOrderByCreatedAtDesc(Integer roomId);
    boolean existsByRoom_IdAndUser_Id(Integer roomId, Integer userId);
}
