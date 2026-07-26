package cv.terrasystem.zebratravelb.booking;

import cv.terrasystem.zebratravelb.common.BadRequestException;
import cv.terrasystem.zebratravelb.common.NotFoundException;
import cv.terrasystem.zebratravelb.excursion.Excursion;
import cv.terrasystem.zebratravelb.excursion.ExcursionRepository;
import cv.terrasystem.zebratravelb.security.UserPrincipal;
import cv.terrasystem.zebratravelb.tour.Tour;
import cv.terrasystem.zebratravelb.tour.TourRepository;
import cv.terrasystem.zebratravelb.user.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingRepository bookingRepository;
    private final ExcursionRepository excursionRepository;
    private final TourRepository tourRepository;

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
        boolean hasExcursion = request.excursionSlug() != null && !request.excursionSlug().isBlank();
        boolean hasTour = request.tourId() != null;
        if (hasExcursion == hasTour) {
            throw new BadRequestException("Indica exatamente um de excursionSlug ou tourId");
        }
        if (request.date() == null) {
            throw new BadRequestException("Data é obrigatória");
        }
        if (request.date().isBefore(LocalDate.now())) {
            throw new BadRequestException("A data não pode ser no passado");
        }

        int guests = request.guests() != null && request.guests() > 0 ? request.guests() : 1;

        Booking booking = new Booking();
        booking.setUser(principal.getUser());
        booking.setBookingDate(request.date());
        booking.setStatus("PENDING");

        if (hasExcursion) {
            Excursion excursion = excursionRepository.findBySlug(request.excursionSlug())
                    .orElseThrow(() -> new NotFoundException("Excursão não encontrada: " + request.excursionSlug()));
            booking.setExcursion(excursion);
            booking.setItemName(excursion.getTitle());
            booking.setAmount(excursion.getPrice().multiply(BigDecimal.valueOf(guests)));
        } else {
            Tour tour = tourRepository.findById(request.tourId())
                    .orElseThrow(() -> new NotFoundException("Destino não encontrado: " + request.tourId()));
            booking.setTour(tour);
            booking.setItemName(tour.getTitle());
            booking.setAmount(tour.getPrice().multiply(BigDecimal.valueOf(guests)));
        }

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
