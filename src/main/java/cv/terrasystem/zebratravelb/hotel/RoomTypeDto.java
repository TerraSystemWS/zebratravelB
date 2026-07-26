package cv.terrasystem.zebratravelb.hotel;

import java.math.BigDecimal;

public record RoomTypeDto(
        Integer id,
        Integer hotelId,
        String name,
        String description,
        BigDecimal basePrice,
        Integer capacity,
        String image,
        Integer createdBy
) {
    public static RoomTypeDto from(HotelRoomType rt) {
        return new RoomTypeDto(
                rt.getId(), rt.getHotel().getId(), rt.getName(), rt.getDescription(), rt.getBasePrice(), rt.getCapacity(), rt.getImage(),
                rt.getCreatedBy() != null ? rt.getCreatedBy().getId() : null
        );
    }

    public void applyTo(HotelRoomType rt) {
        rt.setName(name);
        rt.setDescription(description);
        rt.setBasePrice(basePrice);
        rt.setCapacity(capacity != null ? capacity : 2);
        rt.setImage(image);
    }
}
