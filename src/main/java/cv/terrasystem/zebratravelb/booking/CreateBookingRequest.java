package cv.terrasystem.zebratravelb.booking;

import java.time.LocalDate;

public record CreateBookingRequest(
        String excursionSlug,
        Integer tourId,
        LocalDate date,
        Integer guests,
        String paymentMethod,  // obrigatório só para excursões (ONLINE/TRANSFER/CASH) — Destinos (Tour) continuam sem gateway de pagamento
        String voucherCode     // opcional, só para excursões (Destinos não têm promoções/vouchers)
) {
}
