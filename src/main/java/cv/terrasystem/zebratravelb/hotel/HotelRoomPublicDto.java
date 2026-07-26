package cv.terrasystem.zebratravelb.hotel;

import java.math.BigDecimal;
import java.util.List;

public record HotelRoomPublicDto(
        Integer id,
        String roomNumber,
        String floor,
        String status,
        List<String> amenities,
        List<String> images,
        Integer roomTypeId,
        String roomTypeName,
        String roomTypeDescription,
        String roomTypeImage,
        BigDecimal basePrice,
        Integer capacity,
        Integer hotelId,
        String hotelName,
        String hotelImage
) {
    public static HotelRoomPublicDto from(HotelRoom room) {
        HotelRoomType rt = room.getRoomType();
        Hotel hotel = rt.getHotel();
        return new HotelRoomPublicDto(
                room.getId(),
                room.getRoomNumber(),
                room.getFloor(),
                room.getStatus(),
                room.getAmenities() != null ? List.of(room.getAmenities()) : List.of(),
                room.getImages() != null ? List.of(room.getImages()) : List.of(),
                rt.getId(),
                rt.getName(),
                rt.getDescription(),
                rt.getImage(),
                rt.getBasePrice(),
                rt.getCapacity(),
                hotel.getId(),
                hotel.getName(),
                hotel.getImage()
        );
    }
}
