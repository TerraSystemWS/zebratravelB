package cv.terrasystem.zebratravelb.hotel;

public record HotelDto(Integer id, String name, String address, String city, String description, String image) {
    public static HotelDto from(Hotel hotel) {
        return new HotelDto(hotel.getId(), hotel.getName(), hotel.getAddress(), hotel.getCity(), hotel.getDescription(), hotel.getImage());
    }

    public void applyTo(Hotel hotel) {
        hotel.setName(name);
        hotel.setAddress(address);
        hotel.setCity(city);
        hotel.setDescription(description);
        hotel.setImage(image);
    }
}
