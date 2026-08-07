package cv.terrasystem.zebratravelb.hotel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HotelRoomRepository extends JpaRepository<HotelRoom, Integer> {
    List<HotelRoom> findByRoomType_Id(Integer roomTypeId);
    List<HotelRoom> findByRoomType_Hotel_Id(Integer hotelId);
    boolean existsByHotel_IdAndRoomNumberIgnoreCase(Integer hotelId, String roomNumber);
    boolean existsByHotel_IdAndRoomNumberIgnoreCaseAndIdNot(Integer hotelId, String roomNumber, Integer id);

    @Query(value = "SELECT * FROM hotel_rooms WHERE EXISTS (SELECT 1 FROM unnest(images) img WHERE img LIKE CONCAT('%', :needle, '%'))", nativeQuery = true)
    List<HotelRoom> findByImagesArrayContaining(@Param("needle") String needle);
}
