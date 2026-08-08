package cv.terrasystem.zebratravelb.excursion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ExcursionGroupDto(
        Integer id,
        String excursionSlug,
        String excursionTitle,
        String image,
        BigDecimal price,
        String duration,
        String location,
        String description,
        String status,
        LocalDate confirmedDate,
        LocalDateTime createdAt,
        Integer createdById
) {
    public static ExcursionGroupDto from(ExcursionGroup g) {
        Excursion e = g.getExcursion();
        return new ExcursionGroupDto(
                g.getId(), e.getSlug(), e.getTitle(), e.getImage(), e.getPrice(),
                e.getDuration(), e.getLocation(), e.getDescription(),
                g.getStatus(), g.getConfirmedDate(), g.getCreatedAt(),
                e.getCreatedBy() != null ? e.getCreatedBy().getId() : null
        );
    }
}
