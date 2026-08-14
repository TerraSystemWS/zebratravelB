package cv.terrasystem.zebratravelb.booking;

import cv.terrasystem.zebratravelb.common.BadRequestException;
import cv.terrasystem.zebratravelb.common.NotFoundException;
import cv.terrasystem.zebratravelb.excursion.Excursion;
import cv.terrasystem.zebratravelb.excursion.ExcursionGroup;
import cv.terrasystem.zebratravelb.excursion.ExcursionGroupRepository;
import cv.terrasystem.zebratravelb.excursion.ExcursionRepository;
import cv.terrasystem.zebratravelb.invoice.InvoiceService;
import cv.terrasystem.zebratravelb.notification.Notification;
import cv.terrasystem.zebratravelb.notification.NotificationService;
import cv.terrasystem.zebratravelb.security.UserPrincipal;
import cv.terrasystem.zebratravelb.tour.Tour;
import cv.terrasystem.zebratravelb.tour.TourRepository;
import cv.terrasystem.zebratravelb.user.Role;
import cv.terrasystem.zebratravelb.voucher.Voucher;
import cv.terrasystem.zebratravelb.voucher.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private static final Set<String> PAYMENT_METHODS = Set.of(Booking.ONLINE, Booking.TRANSFER, Booking.CASH);
    // Reservas ainda "ativas" neste grupo — um cliente com uma destas não pode voltar a
    // reservar o mesmo grupo (ver dedupe em create()). Não bloqueia grupos diferentes da
    // mesma excursão (ex: um grupo já CONFIRMED/COMPLETED anterior).
    private static final List<String> ACTIVE_GROUP_STATUSES = List.of(Booking.PENDING, Booking.PENDING_PAYMENT,
            Booking.AWAITING_TRANSFER, Booking.AWAITING_CASH, Booking.CONFIRMED);

    private final BookingRepository bookingRepository;
    private final ExcursionRepository excursionRepository;
    private final ExcursionGroupRepository excursionGroupRepository;
    private final TourRepository tourRepository;
    private final VoucherService voucherService;
    private final NotificationService notificationService;
    private final InvoiceService invoiceService;

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
        booking.setCustomerNif(request.customerNif());

        Voucher voucher = null;
        BigDecimal discountAmount = BigDecimal.ZERO;

        if (hasExcursion) {
            // Só Excursões passam pelo gateway de pagamento — Destinos (Tour) continuam PENDING
            // simples, resolvido manualmente pelo admin, sem alterações a esse fluxo.
            String paymentMethod = request.paymentMethod();
            if (paymentMethod == null || !PAYMENT_METHODS.contains(paymentMethod.toUpperCase())) {
                throw new BadRequestException("Método de pagamento inválido");
            }
            paymentMethod = paymentMethod.toUpperCase();

            Excursion excursion = excursionRepository.findBySlug(request.excursionSlug())
                    .orElseThrow(() -> new NotFoundException("Excursão não encontrada: " + request.excursionSlug()));
            booking.setExcursion(excursion);
            booking.setItemName(excursion.getTitle());
            BigDecimal baseAmount = excursion.getPrice().multiply(BigDecimal.valueOf(guests));
            if (request.voucherCode() != null && !request.voucherCode().isBlank()) {
                voucher = voucherService.validateCode(request.voucherCode(), Voucher.EXCURSION, excursion.getId(), principal.getUser());
            }
            BigDecimal finalAmount = voucher != null ? voucherService.applyDiscount(voucher, baseAmount) : baseAmount;
            discountAmount = baseAmount.subtract(finalAmount);
            booking.setAmount(finalAmount);
            // Uma excursão confirmada não pode receber mais reservas no mesmo grupo —
            // se já não há nenhum grupo OPEN (porque o único existente está CONFIRMED
            // ou COMPLETED, ou porque é a primeira reserva de sempre), abre-se um novo.
            ExcursionGroup group = excursionGroupRepository
                    .findFirstByExcursion_IdAndStatus(excursion.getId(), "OPEN")
                    .orElseGet(() -> {
                        ExcursionGroup g = new ExcursionGroup();
                        g.setExcursion(excursion);
                        g.setStatus("OPEN");
                        return excursionGroupRepository.save(g);
                    });
            // Um cliente não pode reservar duas vezes o mesmo grupo de viagem — não bloqueia
            // reservar a mesma excursão outra vez num grupo diferente (futuro, já confirmado/completado).
            if (bookingRepository.existsByExcursionGroup_IdAndUser_IdAndStatusIn(group.getId(), principal.getId(), ACTIVE_GROUP_STATUSES)) {
                throw new BadRequestException("Já tens uma reserva neste grupo de viagem");
            }
            booking.setExcursionGroup(group);
            booking.setPaymentMethod(paymentMethod);
            booking.setStatus(switch (paymentMethod) {
                case Booking.TRANSFER -> Booking.AWAITING_TRANSFER;
                case Booking.CASH -> Booking.AWAITING_CASH;
                default -> Booking.PENDING_PAYMENT;
            });
        } else {
            Tour tour = tourRepository.findById(request.tourId())
                    .orElseThrow(() -> new NotFoundException("Destino não encontrado: " + request.tourId()));
            booking.setTour(tour);
            booking.setItemName(tour.getTitle());
            booking.setAmount(tour.getPrice().multiply(BigDecimal.valueOf(guests)));
            booking.setStatus(Booking.PENDING);
        }

        Booking saved = bookingRepository.save(booking);
        if (voucher != null) {
            voucherService.recordRedemption(voucher, principal.getUser(), discountAmount, saved, null, null);
        }
        if (hasExcursion) {
            notificationService.notify(Notification.EXCURSION_BOOKING, "Nova reserva de excursão",
                    saved.getItemName(), "/dashboard/bookings", saved.getId());
        }
        return BookingDto.from(saved);
    }

    @PatchMapping("/{id}/status")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public BookingDto updateStatus(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Reserva não encontrada: " + id));
        String status = body.get("status").toUpperCase();
        booking.setStatus(status);
        Booking saved = bookingRepository.save(booking);
        if (Booking.CANCELLED.equals(status)) {
            voucherService.releaseForBooking(saved.getId());
        } else if (Booking.CONFIRMED.equals(status)) {
            // Cobre a confirmação manual de pagamento por transferência/dinheiro — o pagamento
            // online já emite a fatura a partir de PaymentController.callback(); issueForBooking()
            // é idempotente, por isso não há risco de duplicar se o mesmo booking já a tiver.
            invoiceService.issueForBooking(saved);
        }
        return BookingDto.from(saved);
    }
}
