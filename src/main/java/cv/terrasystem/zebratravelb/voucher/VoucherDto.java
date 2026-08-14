package cv.terrasystem.zebratravelb.voucher;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record VoucherDto(
        Integer id,
        String code,
        boolean requiresCode,
        Integer discountPercent,
        String scope,
        Integer scopeItemId,
        LocalDate validFrom,
        LocalDate validUntil,
        Integer maxUses,
        Integer maxUsesPerUser,
        boolean active,
        Integer createdById,
        LocalDateTime createdAt,
        long usesCount
) {
    public static VoucherDto from(Voucher v, long usesCount) {
        return new VoucherDto(
                v.getId(), v.getCode(), v.isRequiresCode(), v.getDiscountPercent(), v.getScope(), v.getScopeItemId(),
                v.getValidFrom(), v.getValidUntil(), v.getMaxUses(), v.getMaxUsesPerUser(), v.isActive(),
                v.getCreatedBy() != null ? v.getCreatedBy().getId() : null, v.getCreatedAt(), usesCount
        );
    }
}
