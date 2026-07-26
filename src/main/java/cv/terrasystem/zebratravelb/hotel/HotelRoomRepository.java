package cv.terrasystem.zebratravelb.hotel;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HotelRoomRepository extends JpaRepository<HotelRoom, Integer> {
    List<HotelRoom> findByRoomType_Id(Integer roomTypeId);
    List<HotelRoom> findByRoomType_Hotel_Id(Integer hotelId);
}
