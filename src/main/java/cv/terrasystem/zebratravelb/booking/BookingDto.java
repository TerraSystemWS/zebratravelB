package cv.terrasystem.zebratravelb.booking;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BookingDto(
        Integer id,
        String user,
        String userEmail,
        String item,
        LocalDate date,
        String status,
        BigDecimal amount,
        String excursionSlug,
        Integer excursionGroupId,
        Integer tourId,
        String type
) {
    public static BookingDto from(Booking b) {
        return new BookingDto(
                b.getId(),
                b.getUser() != null
                        ? (b.getUser().getFullName() != null ? b.getUser().getFullName() : b.getUser().getUsername())
                        : (b.getGuestName() != null ? b.getGuestName() : "N/A"),
                b.getUser() != null ? b.getUser().getEmail() : b.getGuestEmail(),
                b.getItemName(),
                b.getBookingDate(),
                b.getStatus(),
                b.getAmount(),
                b.getExcursion() != null ? b.getExcursion().getSlug() : null,
                b.getExcursionGroup() != null ? b.getExcursionGroup().getId() : null,
                b.getTour() != null ? b.getTour().getId() : null,
                b.getExcursion() != null ? "EXCURSION" : "TOUR"
        );
    }
}
