package cv.terrasystem.zebratravelb.booking;

import java.time.LocalDate;

public record CreateBookingRequest(
        String excursionSlug,
        Integer tourId,
        LocalDate date,
        Integer guests
) {
}
