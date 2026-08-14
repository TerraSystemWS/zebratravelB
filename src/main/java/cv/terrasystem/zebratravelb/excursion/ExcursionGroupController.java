package cv.terrasystem.zebratravelb.excursion;

import cv.terrasystem.zebratravelb.booking.Booking;
import cv.terrasystem.zebratravelb.booking.BookingDto;
import cv.terrasystem.zebratravelb.booking.BookingRepository;
import cv.terrasystem.zebratravelb.common.BadRequestException;
import cv.terrasystem.zebratravelb.common.ConflictException;
import cv.terrasystem.zebratravelb.common.NotFoundException;
import cv.terrasystem.zebratravelb.common.OwnershipGuard;
import cv.terrasystem.zebratravelb.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

// Gestão dos grupos de viagem (cada excursão pode ter vários ao longo do
// tempo — ver BookingController.create, que abre um grupo novo sempre que
// já não há nenhum OPEN para a excursão). A listagem pública dos grupos
// confirmados continua em ExcursionController (GET /api/excursions/group-travel).
@RestController
@RequestMapping("/api/excursion-groups")
@RequiredArgsConstructor
public class ExcursionGroupController {

    private static final Set<String> BOOKING_STATUSES = Set.of("PENDING", "CONFIRMED", "CANCELLED");

    private final ExcursionGroupRepository excursionGroupRepository;
    private final ExcursionRepository excursionRepository;
    private final BookingRepository bookingRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public List<ExcursionGroupDto> getAll() {
        return excursionGroupRepository.findAll().stream().map(ExcursionGroupDto::from).toList();
    }

    // Inicia manualmente um grupo de viagem para uma excursão (sem depender de uma reserva de
    // cliente para o criar). Se já houver um grupo OPEN para esta excursão, devolve esse mesmo
    // grupo em vez de criar um duplicado — mantém a invariante de "no máximo um grupo OPEN por
    // excursão" já garantida por BookingController.create.
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public ExcursionGroupDto create(@AuthenticationPrincipal UserPrincipal principal, @RequestBody CreateGroupRequest request) {
        if (request.excursionSlug() == null || request.excursionSlug().isBlank()) {
            throw new BadRequestException("excursionSlug é obrigatório");
        }
        Excursion excursion = excursionRepository.findBySlug(request.excursionSlug())
                .orElseThrow(() -> new NotFoundException("Excursão não encontrada: " + request.excursionSlug()));
        OwnershipGuard.requireOwnerOrAdmin(principal, excursion.getCreatedBy());

        ExcursionGroup existing = excursionGroupRepository
                .findFirstByExcursion_IdAndStatus(excursion.getId(), "OPEN")
                .orElse(null);
        if (existing != null) {
            return ExcursionGroupDto.from(existing);
        }
        ExcursionGroup group = new ExcursionGroup();
        group.setExcursion(excursion);
        group.setStatus("OPEN");
        return ExcursionGroupDto.from(excursionGroupRepository.save(group));
    }

    // Participante adicionado manualmente (sem conta de cliente) por ADMIN/AGENTE — o grupo
    // continua OPEN, por isso outros clientes podem juntar-se pelo site público entretanto
    // (mesmo mecanismo de BookingController.create, que anexa ao mesmo grupo OPEN).
    @PostMapping("/{id}/participants")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public BookingDto addParticipant(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Integer id,
                                      @RequestBody AddGroupParticipantRequest request) {
        ExcursionGroup group = find(id);
        OwnershipGuard.requireOwnerOrAdmin(principal, group.getExcursion().getCreatedBy());
        if (!"OPEN".equals(group.getStatus())) {
            throw new BadRequestException("Só é possível adicionar participantes a um grupo em aberto");
        }
        if (request.guestName() == null || request.guestName().isBlank()) {
            throw new BadRequestException("Nome do participante é obrigatório");
        }

        Booking booking = new Booking();
        booking.setExcursion(group.getExcursion());
        booking.setExcursionGroup(group);
        booking.setGuestName(request.guestName());
        booking.setGuestEmail(request.guestEmail());
        booking.setGuestPhone(request.guestPhone());
        booking.setItemName(group.getExcursion().getTitle());
        booking.setBookingDate(request.date() != null ? request.date() : LocalDate.now());
        booking.setAmount(group.getExcursion().getPrice());

        String status = request.status();
        if (status != null && !status.isBlank()) {
            if (!BOOKING_STATUSES.contains(status.toUpperCase())) {
                throw new BadRequestException("Estado inválido: " + status);
            }
            booking.setStatus(status.toUpperCase());
        } else {
            booking.setStatus("PENDING");
        }

        return BookingDto.from(bookingRepository.save(booking));
    }

    // Bloqueia sempre a remoção de quem já pagou (status CONFIRMED = "confirmado e pago", ver
    // Booking.java) — mesma regra incondicional (nem ADMIN pode) de HotelReservationController.delete
    // para reservas confirmadas.
    @DeleteMapping("/{id}/participants/{bookingId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public ResponseEntity<Void> removeParticipant(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Integer id,
                                                   @PathVariable Integer bookingId) {
        ExcursionGroup group = find(id);
        OwnershipGuard.requireOwnerOrAdmin(principal, group.getExcursion().getCreatedBy());
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Participante não encontrado: " + bookingId));
        if (booking.getExcursionGroup() == null || !booking.getExcursionGroup().getId().equals(id)) {
            throw new NotFoundException("Participante não encontrado: " + bookingId);
        }
        if ("CONFIRMED".equals(booking.getStatus())) {
            throw new ConflictException("Não é possível remover um participante já confirmado/pago — cancele-o em vez disso.");
        }
        bookingRepository.delete(booking);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public ExcursionGroupDto confirm(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Integer id, @RequestBody ConfirmGroupTravelRequest request) {
        ExcursionGroup group = find(id);
        OwnershipGuard.requireOwnerOrAdmin(principal, group.getExcursion().getCreatedBy());
        if (request.confirmedDate() == null) {
            throw new BadRequestException("confirmedDate é obrigatória");
        }
        if (request.confirmedDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("A data confirmada não pode ser no passado");
        }
        group.setStatus("CONFIRMED");
        group.setConfirmedDate(request.confirmedDate());
        return ExcursionGroupDto.from(excursionGroupRepository.save(group));
    }

    @PostMapping("/{id}/reopen")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public ExcursionGroupDto reopen(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Integer id) {
        ExcursionGroup group = find(id);
        OwnershipGuard.requireOwnerOrAdmin(principal, group.getExcursion().getCreatedBy());
        group.setStatus("OPEN");
        group.setConfirmedDate(null);
        return ExcursionGroupDto.from(excursionGroupRepository.save(group));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public ExcursionGroupDto complete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Integer id) {
        ExcursionGroup group = find(id);
        OwnershipGuard.requireOwnerOrAdmin(principal, group.getExcursion().getCreatedBy());
        if (!"CONFIRMED".equals(group.getStatus())) {
            throw new BadRequestException("Só é possível marcar como terminado um grupo confirmado.");
        }
        if (group.getConfirmedDate() == null || group.getConfirmedDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("O grupo só pode ser marcado como terminado depois da data confirmada.");
        }
        group.setStatus("COMPLETED");
        return ExcursionGroupDto.from(excursionGroupRepository.save(group));
    }

    private ExcursionGroup find(Integer id) {
        return excursionGroupRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Grupo não encontrado: " + id));
    }
}
