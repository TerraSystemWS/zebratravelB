package cv.terrasystem.zebratravelb.hotel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface HotelRoomReviewRepository extends JpaRepository<HotelRoomReview, Integer> {
    List<HotelRoomReview> findByRoom_IdOrderByCreatedAtDesc(Integer roomId);
    boolean existsByRoom_IdAndUser_Id(Integer roomId, Integer userId);
    boolean existsByUser_Id(Integer userId);

    @Query("select coalesce(sum(r.rating), 0) from HotelRoomReview r")
    long sumRating();
}
