package cv.terrasystem.zebratravelb.campaign;

import java.time.LocalDate;

public record CampaignRequest(
        String name,
        String imageUrl,
        String altText,
        String placement,
        Integer voucherId,
        Integer productId,
        Integer excursionId,
        Integer roomTypeId,
        String title,
        String subtitle,
        String linkUrl,
        LocalDate startDate,
        LocalDate endDate,
        Integer priority,
        Boolean active
) {
}
