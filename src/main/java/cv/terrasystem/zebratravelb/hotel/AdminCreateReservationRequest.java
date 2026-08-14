package cv.terrasystem.zebratravelb.hotel;

import java.time.LocalDate;

public record AdminCreateReservationRequest(
        Integer roomId,
        String guestName,
        String guestEmail,
        String guestPhone,
        LocalDate checkIn,
        LocalDate checkOut,
        Integer guests,
        String paymentMethod,
        String status,   // optional initial status, e.g. ON_HOLD or CONFIRMED - only this admin endpoint may set it
        String voucherCode   // opcional
) {
}
