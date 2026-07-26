package cv.terrasystem.zebratravelb.hotel;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HotelRoomTypeRepository extends JpaRepository<HotelRoomType, Integer> {
    List<HotelRoomType> findByHotel_Id(Integer hotelId);
}
