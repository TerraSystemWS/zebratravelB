package cv.terrasystem.zebratravelb.voucher;

import java.time.LocalDate;

public record VoucherRequest(
        String code,               // obrigatório se requiresCode=true, ignorado se false
        Boolean requiresCode,
        Integer discountPercent,   // 0-99
        String scope,              // ALL, EXCURSION, ROOM, PRODUCT
        Integer scopeItemId,       // obrigatório quando requiresCode=false (promoção fixa)
        LocalDate validFrom,
        LocalDate validUntil,
        Integer maxUses,
        Integer maxUsesPerUser,
        Boolean active
) {
}
