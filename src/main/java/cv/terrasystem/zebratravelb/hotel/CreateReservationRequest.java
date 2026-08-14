package cv.terrasystem.zebratravelb.hotel;

import java.time.LocalDate;

public record CreateReservationRequest(
        Integer roomId,
        LocalDate checkIn,
        LocalDate checkOut,
        Integer guests,
        String paymentMethod,
        String voucherCode,  // opcional
        String customerNif   // opcional — sem ele, a fatura sai como "Consumidor Final"
) {
}
