package cv.terrasystem.zebratravelb.hotel;

public record RoomDto(Integer id, Integer roomTypeId, String roomNumber, String floor, String status, String[] images, String[] amenities, Integer createdBy) {
    public static RoomDto from(HotelRoom room) {
        return new RoomDto(room.getId(), room.getRoomType().getId(), room.getRoomNumber(), room.getFloor(), room.getStatus(), room.getImages(), room.getAmenities(),
                room.getCreatedBy() != null ? room.getCreatedBy().getId() : null);
    }

    public void applyTo(HotelRoom room) {
        room.setRoomNumber(roomNumber);
        room.setFloor(floor);
        room.setStatus(status != null ? status : "ACTIVE");
        room.setImages(images != null ? images : new String[0]);
        room.setAmenities(amenities != null ? amenities : new String[0]);
    }
}
