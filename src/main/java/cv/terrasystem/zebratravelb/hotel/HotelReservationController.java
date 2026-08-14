package cv.terrasystem.zebratravelb.hotel;

import cv.terrasystem.zebratravelb.common.BadRequestException;
import cv.terrasystem.zebratravelb.common.ConflictException;
import cv.terrasystem.zebratravelb.common.NotFoundException;
import cv.terrasystem.zebratravelb.invoice.InvoiceService;
import cv.terrasystem.zebratravelb.notification.Notification;
import cv.terrasystem.zebratravelb.notification.NotificationService;
import cv.terrasystem.zebratravelb.security.UserPrincipal;
import cv.terrasystem.zebratravelb.user.Role;
import cv.terrasystem.zebratravelb.user.User;
import cv.terrasystem.zebratravelb.voucher.Voucher;
import cv.terrasystem.zebratravelb.voucher.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/hotel/reservations")
@RequiredArgsConstructor
public class HotelReservationController {

    private static final Set<String> PAYMENT_METHODS = Set.of(
            HotelReservation.ONLINE, HotelReservation.TRANSFER, HotelReservation.CASH
    );
    private static final Set<String> ADMIN_SETTABLE_STATUSES = Set.of(
            HotelReservation.ON_HOLD, HotelReservation.CONFIRMED, HotelReservation.CANCELLED
    );
    private static final Set<String> CHECK_IN_ELIGIBLE_STATUSES = Set.of(
            HotelReservation.CONFIRMED
    );
    // Reservas ainda "não pagas" — o único conjunto que um AGENTE (não-admin) pode apagar.
    private static final Set<String> NOT_YET_PAID_STATUSES = Set.of(
            HotelReservation.PENDING_PAYMENT, HotelReservation.AWAITING_TRANSFER, HotelReservation.AWAITING_CASH,
            HotelReservation.CANCELLED, HotelReservation.FAILED
    );

    private final HotelReservationRepository reservationRepository;
    private final HotelRoomRepository roomRepository;
    private final VoucherService voucherService;
    private final NotificationService notificationService;
    private final InvoiceService invoiceService;

    @GetMapping("/mine")
    public List<ReservationDto> getMine(@AuthenticationPrincipal UserPrincipal principal) {
        return reservationRepository.findByUser_IdOrderByCreatedAtDesc(principal.getId()).stream()
                .map(ReservationDto::from).toList();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public List<ReservationDto> getForHotel(
            @RequestParam Integer hotelId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to
    ) {
        LocalDate rangeStart = from != null ? from : LocalDate.now().minusMonths(1);
        LocalDate rangeEnd = to != null ? to : LocalDate.now().plusMonths(2);
        return reservationRepository.findByHotel_IdAndCheckInLessThanAndCheckOutGreaterThan(hotelId, rangeEnd, rangeStart)
                .stream().map(ReservationDto::from).toList();
    }

    @PostMapping
    public ReservationDto create(@AuthenticationPrincipal UserPrincipal principal, @RequestBody CreateReservationRequest request) {
        HotelRoom room = requireRoom(request.roomId());
        validateDates(request.checkIn(), request.checkOut());
        if (request.checkIn().isBefore(LocalDate.now())) {
            throw new BadRequestException("A data de check-in não pode ser no passado");
        }
        ensureAvailable(room, request.checkIn(), request.checkOut(), null);
        validatePaymentMethod(request.paymentMethod());

        HotelReservation reservation = new HotelReservation();
        reservation.setHotel(room.getRoomType().getHotel());
        reservation.setRoom(room);
        reservation.setUser(principal.getUser());
        reservation.setCheckIn(request.checkIn());
        reservation.setCheckOut(request.checkOut());
        reservation.setGuests(resolveGuests(room, request.guests()));
        reservation.setPaymentMethod(request.paymentMethod().toUpperCase());
        reservation.setCustomerNif(request.customerNif());
        reservation.setStatus(switch (reservation.getPaymentMethod()) {
            case HotelReservation.TRANSFER -> HotelReservation.AWAITING_TRANSFER;
            case HotelReservation.CASH -> HotelReservation.AWAITING_CASH;
            default -> HotelReservation.PENDING_PAYMENT;
        });

        BigDecimal baseAmount = computeTotal(room, request.checkIn(), request.checkOut());
        Voucher voucher = resolveVoucher(request.voucherCode(), room, principal.getUser());
        BigDecimal finalAmount = voucher != null ? voucherService.applyDiscount(voucher, baseAmount) : baseAmount;
        reservation.setTotalAmount(finalAmount);

        HotelReservation saved = reservationRepository.save(reservation);
        if (voucher != null) {
            voucherService.recordRedemption(voucher, principal.getUser(), baseAmount.subtract(finalAmount), null, saved, null);
        }
        notificationService.notify(Notification.HOTEL_RESERVATION, "Nova reserva de quarto",
                saved.getHotel().getName() + " — " + room.getRoomType().getName(),
                "/dashboard/hotel/reservas", saved.getId());
        return ReservationDto.from(saved);
    }

    @PostMapping("/admin")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public ReservationDto createAsAdmin(@AuthenticationPrincipal UserPrincipal principal, @RequestBody AdminCreateReservationRequest request) {
        HotelRoom room = requireRoom(request.roomId());
        validateDates(request.checkIn(), request.checkOut());
        ensureAvailable(room, request.checkIn(), request.checkOut(), null);
        validatePaymentMethod(request.paymentMethod());

        if (request.guestName() == null || request.guestName().isBlank()) {
            throw new BadRequestException("Nome do hóspede é obrigatório");
        }

        HotelReservation reservation = new HotelReservation();
        reservation.setHotel(room.getRoomType().getHotel());
        reservation.setRoom(room);
        reservation.setGuestName(request.guestName());
        reservation.setGuestEmail(request.guestEmail());
        reservation.setGuestPhone(request.guestPhone());
        reservation.setCheckIn(request.checkIn());
        reservation.setCheckOut(request.checkOut());
        reservation.setGuests(resolveGuests(room, request.guests()));
        reservation.setPaymentMethod(request.paymentMethod().toUpperCase());
        reservation.setCustomerNif(request.customerNif());
        reservation.setCreatedBy(principal.getUser());

        String status = request.status();
        if (status != null && !status.isBlank()) {
            validateAdminSettableStatus(status);
            reservation.setStatus(status.toUpperCase());
        } else {
            reservation.setStatus(HotelReservation.CONFIRMED);
        }

        BigDecimal baseAmount = computeTotal(room, request.checkIn(), request.checkOut());
        Voucher voucher = resolveVoucher(request.voucherCode(), room, principal.getUser());
        BigDecimal finalAmount = voucher != null ? voucherService.applyDiscount(voucher, baseAmount) : baseAmount;
        reservation.setTotalAmount(finalAmount);

        HotelReservation saved = reservationRepository.save(reservation);
        if (voucher != null) {
            voucherService.recordRedemption(voucher, principal.getUser(), baseAmount.subtract(finalAmount), null, saved, null);
        }
        notificationService.notify(Notification.HOTEL_RESERVATION, "Nova reserva de quarto",
                saved.getHotel().getName() + " — " + room.getRoomType().getName(),
                "/dashboard/hotel/reservas", saved.getId());
        return ReservationDto.from(saved);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public ReservationDto updateStatus(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        HotelReservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Reserva não encontrada: " + id));
        requireNotCheckedOut(reservation);
        String status = body.get("status");
        validateAdminSettableStatus(status);
        reservation.setStatus(status.toUpperCase());
        HotelReservation saved = reservationRepository.save(reservation);
        if (HotelReservation.CANCELLED.equals(saved.getStatus())) {
            voucherService.releaseForHotelReservation(saved.getId());
        } else if (HotelReservation.CONFIRMED.equals(saved.getStatus())) {
            // Cobre a confirmação manual de pagamento por transferência/dinheiro — o pagamento
            // online já emite a fatura a partir de PaymentController.callback();
            // issueForHotelReservation() é idempotente, sem risco de duplicar.
            invoiceService.issueForHotelReservation(saved);
        }
        return ReservationDto.from(saved);
    }

    private void requireNotCheckedOut(HotelReservation reservation) {
        if (reservation.getCheckedOutAt() != null) {
            throw new BadRequestException("Esta reserva já teve check-out — não há mais operações possíveis sobre ela");
        }
    }

    private void validateAdminSettableStatus(String status) {
        if (status == null || !ADMIN_SETTABLE_STATUSES.contains(status.toUpperCase())) {
            throw new BadRequestException("Estado inválido: " + status);
        }
    }

    @PatchMapping("/{id}/dates")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public ReservationDto moveDates(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        HotelReservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Reserva não encontrada: " + id));
        requireNotCheckedOut(reservation);
        if (reservation.getRoom() == null) {
            throw new BadRequestException("Esta reserva não tem quarto associado");
        }
        LocalDate checkIn = LocalDate.parse(body.get("checkIn"));
        LocalDate checkOut = LocalDate.parse(body.get("checkOut"));
        validateDates(checkIn, checkOut);
        ensureAvailable(reservation.getRoom(), checkIn, checkOut, id);

        reservation.setCheckIn(checkIn);
        reservation.setCheckOut(checkOut);
        reservation.setTotalAmount(computeTotal(reservation.getRoom(), checkIn, checkOut));
        return ReservationDto.from(reservationRepository.save(reservation));
    }

    @PatchMapping("/{id}/checkin")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public ReservationDto checkIn(@PathVariable Integer id) {
        HotelReservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Reserva não encontrada: " + id));
        if (!CHECK_IN_ELIGIBLE_STATUSES.contains(reservation.getStatus())) {
            throw new BadRequestException("Só é possível fazer check-in de reservas confirmadas ou pagas");
        }
        if (reservation.getCheckedInAt() != null) {
            throw new BadRequestException("Esta reserva já tem check-in feito");
        }
        reservation.setCheckedInAt(LocalDateTime.now());
        return ReservationDto.from(reservationRepository.save(reservation));
    }

    @PatchMapping("/{id}/checkout")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public ReservationDto checkOut(@PathVariable Integer id) {
        HotelReservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Reserva não encontrada: " + id));
        if (reservation.getCheckedInAt() == null) {
            throw new BadRequestException("Esta reserva ainda não tem check-in feito");
        }
        if (reservation.getCheckedOutAt() != null) {
            throw new BadRequestException("Esta reserva já tem check-out feito");
        }
        reservation.setCheckedOutAt(LocalDateTime.now());
        return ReservationDto.from(reservationRepository.save(reservation));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Integer id) {
        HotelReservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Reserva não encontrada: " + id));
        if (HotelReservation.CONFIRMED.equals(reservation.getStatus())) {
            throw new ConflictException("Não é possível apagar uma reserva confirmada — cancele-a em vez disso.");
        }
        boolean isAdmin = principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !NOT_YET_PAID_STATUSES.contains(reservation.getStatus())) {
            throw new BadRequestException("Só um admin pode apagar uma reserva em espera");
        }
        // Libertar o voucher ANTES de apagar: a FK hotel_reservation_id tem ON DELETE SET
        // NULL, e o delete despoleta um flush automático antes da query de libertação, que
        // deixaria de encontrar a redemption (já com a FK a null) se corresse depois.
        voucherService.releaseForHotelReservation(id);
        reservationRepository.delete(reservation);
        return ResponseEntity.noContent().build();
    }

    private Voucher resolveVoucher(String voucherCode, HotelRoom room, User user) {
        if (voucherCode == null || voucherCode.isBlank()) {
            return null;
        }
        return voucherService.validateCode(voucherCode, Voucher.ROOM, room.getRoomType().getId(), user);
    }

    private int resolveGuests(HotelRoom room, Integer requested) {
        int guests = requested != null && requested > 0 ? requested : 1;
        int capacity = room.getRoomType().getCapacity();
        if (guests > capacity) {
            throw new BadRequestException("Número de hóspedes excede a capacidade do quarto (máx. " + capacity + ")");
        }
        return guests;
    }

    private HotelRoom requireRoom(Integer roomId) {
        if (roomId == null) throw new BadRequestException("roomId é obrigatório");
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("Quarto não encontrado: " + roomId));
    }

    private void validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new BadRequestException("Datas de check-in/check-out são obrigatórias");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new BadRequestException("A data de saída deve ser depois da data de entrada");
        }
    }

    private void ensureAvailable(HotelRoom room, LocalDate checkIn, LocalDate checkOut, Integer ignoreReservationId) {
        boolean taken = reservationRepository.findOverlapping(room.getId(), checkIn, checkOut).stream()
                .anyMatch(r -> ignoreReservationId == null || !r.getId().equals(ignoreReservationId));
        if (taken) {
            throw new BadRequestException("O quarto não está disponível nas datas escolhidas");
        }
    }

    private void validatePaymentMethod(String method) {
        if (method == null || !PAYMENT_METHODS.contains(method.toUpperCase())) {
            throw new BadRequestException("Método de pagamento inválido");
        }
    }

    private BigDecimal computeTotal(HotelRoom room, LocalDate checkIn, LocalDate checkOut) {
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        return room.getRoomType().getBasePrice().multiply(BigDecimal.valueOf(nights));
    }
}
