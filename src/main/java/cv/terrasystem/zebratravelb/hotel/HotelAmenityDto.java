package cv.terrasystem.zebratravelb.hotel;

public record HotelAmenityDto(Integer id, String code, String label, String icon) {
    public static HotelAmenityDto from(HotelAmenity amenity) {
        return new HotelAmenityDto(amenity.getId(), amenity.getCode(), amenity.getLabel(), amenity.getIcon());
    }

    public void applyTo(HotelAmenity amenity) {
        amenity.setCode(code != null ? code.trim().toUpperCase().replace(' ', '_') : null);
        amenity.setLabel(label);
        amenity.setIcon(icon);
    }
}
