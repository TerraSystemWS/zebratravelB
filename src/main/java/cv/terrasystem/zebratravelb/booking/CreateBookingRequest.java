package cv.terrasystem.zebratravelb.booking;

import java.time.LocalDate;

public record CreateBookingRequest(
        String excursionSlug,
        LocalDate date,
        Integer guests
) {
}
