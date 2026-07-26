package cv.terrasystem.zebratravelb.booking;

import cv.terrasystem.zebratravelb.common.BadRequestException;
import cv.terrasystem.zebratravelb.common.NotFoundException;
import cv.terrasystem.zebratravelb.excursion.Excursion;
import cv.terrasystem.zebratravelb.excursion.ExcursionRepository;
import cv.terrasystem.zebratravelb.security.UserPrincipal;
import cv.terrasystem.zebratravelb.user.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingRepository bookingRepository;
    private final ExcursionRepository excursionRepository;

    @GetMapping
    public List<BookingDto> getAll(@AuthenticationPrincipal UserPrincipal principal) {
        boolean isAdmin = Role.ADMIN.equals(principal.getUser().getRole().getName());
        List<Booking> bookings = isAdmin
                ? bookingRepository.findAll()
                : bookingRepository.findByUserId(principal.getId());
        return bookings.stream().map(BookingDto::from).toList();
    }

    @PostMapping
    public BookingDto create(@AuthenticationPrincipal UserPrincipal principal, @RequestBody CreateBookingRequest request) {
        if (request.excursionSlug() == null || request.excursionSlug().isBlank()) {
            throw new BadRequestException("excursionSlug é obrigatório");
        }
        if (request.date() == null) {
            throw new BadRequestException("Data é obrigatória");
        }

        Excursion excursion = excursionRepository.findBySlug(request.excursionSlug())
                .orElseThrow(() -> new NotFoundException("Excursão não encontrada: " + request.excursionSlug()));

        int guests = request.guests() != null && request.guests() > 0 ? request.guests() : 1;

        Booking booking = new Booking();
        booking.setUser(principal.getUser());
        booking.setExcursion(excursion);
        booking.setItemName(excursion.getTitle());
        booking.setBookingDate(request.date());
        booking.setStatus("PENDING");
        booking.setAmount(excursion.getPrice().multiply(BigDecimal.valueOf(guests)));

        return BookingDto.from(bookingRepository.save(booking));
    }

    @PatchMapping("/{id}/status")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public BookingDto updateStatus(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Reserva não encontrada: " + id));
        booking.setStatus(body.get("status").toUpperCase());
        return BookingDto.from(bookingRepository.save(booking));
    }
}
