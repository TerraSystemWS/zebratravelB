package cv.terrasystem.zebratravelb.booking;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BookingDto(
        Integer id,
        String user,
        String item,
        LocalDate date,
        String status,
        BigDecimal amount
) {
    public static BookingDto from(Booking b) {
        return new BookingDto(
                b.getId(),
                b.getUser() != null ? (b.getUser().getFullName() != null ? b.getUser().getFullName() : b.getUser().getUsername()) : "N/A",
                b.getItemName(),
                b.getBookingDate(),
                b.getStatus(),
                b.getAmount()
        );
    }
}
