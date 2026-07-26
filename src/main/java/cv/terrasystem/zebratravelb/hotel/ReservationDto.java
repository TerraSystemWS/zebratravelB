package cv.terrasystem.zebratravelb.hotel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReservationDto(
        Integer id,
        Integer hotelId,
        String hotelName,
        Integer roomId,
        String roomNumber,
        String roomTypeName,
        String roomImage,
        String guestName,
        LocalDate checkIn,
        LocalDate checkOut,
        Integer guests,
        BigDecimal totalAmount,
        String paymentMethod,
        String status,
        LocalDateTime checkedInAt,
        LocalDateTime checkedOutAt
) {
    public static ReservationDto from(HotelReservation r) {
        String guest = r.getUser() != null
                ? (r.getUser().getFullName() != null ? r.getUser().getFullName() : r.getUser().getUsername())
                : r.getGuestName();
        String roomImage = null;
        if (r.getRoom() != null) {
            if (r.getRoom().getImages() != null && r.getRoom().getImages().length > 0) {
                roomImage = r.getRoom().getImages()[0];
            } else {
                roomImage = r.getRoom().getRoomType().getImage();
            }
        }
        return new ReservationDto(
                r.getId(),
                r.getHotel().getId(),
                r.getHotel().getName(),
                r.getRoom() != null ? r.getRoom().getId() : null,
                r.getRoom() != null ? r.getRoom().getRoomNumber() : null,
                r.getRoom() != null ? r.getRoom().getRoomType().getName() : null,
                roomImage,
                guest,
                r.getCheckIn(),
                r.getCheckOut(),
                r.getGuests(),
                r.getTotalAmount(),
                r.getPaymentMethod(),
                r.getStatus(),
                r.getCheckedInAt(),
                r.getCheckedOutAt()
        );
    }
}
