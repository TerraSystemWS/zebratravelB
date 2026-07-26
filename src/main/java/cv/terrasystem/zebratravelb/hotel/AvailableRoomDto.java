package cv.terrasystem.zebratravelb.hotel;

import java.math.BigDecimal;

public record AvailableRoomDto(
        Integer roomId,
        String roomNumber,
        Integer roomTypeId,
        String roomTypeName,
        String roomTypeImage,
        BigDecimal basePrice,
        Integer capacity
) {
    public static AvailableRoomDto from(HotelRoom room) {
        HotelRoomType rt = room.getRoomType();
        return new AvailableRoomDto(room.getId(), room.getRoomNumber(), rt.getId(), rt.getName(), rt.getImage(), rt.getBasePrice(), rt.getCapacity());
    }
}
