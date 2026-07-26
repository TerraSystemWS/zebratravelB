package cv.terrasystem.zebratravelb.hotel;

import java.time.LocalDate;

public record CreateReservationRequest(
        Integer roomId,
        LocalDate checkIn,
        LocalDate checkOut,
        Integer guests,
        String paymentMethod
) {
}
