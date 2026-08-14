package cv.terrasystem.zebratravelb.voucher;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record VoucherRedemptionDto(
        Integer id,
        String userName,
        BigDecimal discountAmount,
        boolean released,
        LocalDateTime redeemedAt,
        String appliedTo
) {
    public static VoucherRedemptionDto from(VoucherRedemption r) {
        String appliedTo;
        if (r.getBooking() != null) {
            appliedTo = "Excursão — reserva #" + r.getBooking().getId();
        } else if (r.getHotelReservation() != null) {
            appliedTo = "Hotel — reserva #" + r.getHotelReservation().getId();
        } else if (r.getOrder() != null) {
            appliedTo = "Loja — encomenda #" + r.getOrder().getId();
        } else {
            appliedTo = "—";
        }
        String userName = r.getUser() != null
                ? (r.getUser().getFullName() != null ? r.getUser().getFullName() : r.getUser().getUsername())
                : "N/A";
        return new VoucherRedemptionDto(r.getId(), userName, r.getDiscountAmount(), r.isReleased(), r.getRedeemedAt(), appliedTo);
    }
}
