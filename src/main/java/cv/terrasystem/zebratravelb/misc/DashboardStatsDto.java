package cv.terrasystem.zebratravelb.misc;

import java.math.BigDecimal;

public record DashboardStatsDto(
        long totalBookings,
        long activeUsers,
        BigDecimal monthlyRevenue,
        Double avgFeedback,
        long feedbackCount
) {
}
