package cv.terrasystem.zebratravelb.misc;

import cv.terrasystem.zebratravelb.booking.BookingRepository;
import cv.terrasystem.zebratravelb.excursion.ExcursionReviewRepository;
import cv.terrasystem.zebratravelb.hotel.HotelReservationRepository;
import cv.terrasystem.zebratravelb.hotel.HotelRoomReviewRepository;
import cv.terrasystem.zebratravelb.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private static final List<String> PAID_STATUSES = List.of("CONFIRMED", "PAID");

    private final BookingRepository bookingRepository;
    private final HotelReservationRepository hotelReservationRepository;
    private final UserRepository userRepository;
    private final ExcursionReviewRepository excursionReviewRepository;
    private final HotelRoomReviewRepository hotelRoomReviewRepository;

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENTE')")
    public DashboardStatsDto getStats() {
        long totalBookings = bookingRepository.count() + hotelReservationRepository.count();
        long activeUsers = userRepository.countByStatus("ACTIVE");

        YearMonth currentMonth = YearMonth.now();
        LocalDateTime start = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime end = LocalDate.now().plusDays(1).atStartOfDay();

        BigDecimal monthlyRevenue = bookingRepository.sumRevenue(PAID_STATUSES, start, end)
                .add(hotelReservationRepository.sumRevenue(PAID_STATUSES, start, end));

        long excursionReviewCount = excursionReviewRepository.count();
        long hotelReviewCount = hotelRoomReviewRepository.count();
        long feedbackCount = excursionReviewCount + hotelReviewCount;

        Double avgFeedback = null;
        if (feedbackCount > 0) {
            long totalRating = excursionReviewRepository.sumRating() + hotelRoomReviewRepository.sumRating();
            avgFeedback = totalRating / (double) feedbackCount;
        }

        return new DashboardStatsDto(totalBookings, activeUsers, monthlyRevenue, avgFeedback, feedbackCount);
    }
}
