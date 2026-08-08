package cv.terrasystem.zebratravelb.excursion;

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

@RestController
@RequestMapping("/api/excursions")
@RequiredArgsConstructor
public class ExcursionController {

    private final ExcursionRepository excursionRepository;
    private final BookingRepository bookingRepository;
    private final ExcursionReviewRepository excursionReviewRepository;

    @GetMapping
    public List<ExcursionDto> getAll(@RequestParam(defaultValue = "false") boolean includeArchived) {
        return excursionRepository.findAll().stream()
                .filter(e -> includeArchived || !"ARCHIVED".equals(e.getStatus()))
                .map(ExcursionDto::from)
                .toList();
    }

    @GetMapping("/group-travel")
    public List<ExcursionDto> getGroupTravel() {
        return excursionRepository.findAll().stream()
                .filter(e -> "CONFIRMED".equals(e.getGroupTravelStatus()) && !"ARCHIVED".equals(e.getStatus()))
                .map(ExcursionDto::from)
                .toList();
    }

    @GetMapping("/{slug}")
    public ExcursionDto getBySlug(@PathVariable String slug) {
        return ExcursionDto.from(find(slug));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public ExcursionDto create(@AuthenticationPrincipal UserPrincipal principal, @RequestBody ExcursionDto dto) {
        Excursion excursion = new Excursion();
        dto.applyTo(excursion);
        excursion.setCreatedBy(principal.getUser());
        return ExcursionDto.from(excursionRepository.save(excursion));
    }

    @PutMapping("/{slug}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public ExcursionDto update(@PathVariable String slug, @RequestBody ExcursionDto dto) {
        Excursion excursion = find(slug);
        dto.applyTo(excursion);
        excursion.setSlug(slug);
        return ExcursionDto.from(excursionRepository.save(excursion));
    }

    @DeleteMapping("/{slug}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable String slug) {
        Excursion excursion = find(slug);
        OwnershipGuard.requireOwnerOrAdmin(principal, excursion.getCreatedBy());
        if (bookingRepository.existsByExcursion_Id(excursion.getId()) || excursionReviewRepository.existsByExcursion_Id(excursion.getId())) {
            throw new ConflictException("Não é possível apagar: existem reservas ou avaliações associadas a esta excursão. Arquive em vez de apagar.");
        }
        excursionRepository.delete(excursion);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{slug}/archive")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public ExcursionDto archive(@AuthenticationPrincipal UserPrincipal principal, @PathVariable String slug) {
        Excursion excursion = find(slug);
        OwnershipGuard.requireOwnerOrAdmin(principal, excursion.getCreatedBy());
        excursion.setStatus("ARCHIVED");
        return ExcursionDto.from(excursionRepository.save(excursion));
    }

    @PostMapping("/{slug}/restore")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public ExcursionDto restore(@AuthenticationPrincipal UserPrincipal principal, @PathVariable String slug) {
        Excursion excursion = find(slug);
        OwnershipGuard.requireOwnerOrAdmin(principal, excursion.getCreatedBy());
        excursion.setStatus("ACTIVE");
        return ExcursionDto.from(excursionRepository.save(excursion));
    }

    @PostMapping("/{slug}/group-travel/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public ExcursionDto confirmGroupTravel(@AuthenticationPrincipal UserPrincipal principal, @PathVariable String slug, @RequestBody ConfirmGroupTravelRequest request) {
        Excursion excursion = find(slug);
        OwnershipGuard.requireOwnerOrAdmin(principal, excursion.getCreatedBy());
        if (request.confirmedDate() == null) {
            throw new BadRequestException("confirmedDate é obrigatória");
        }
        if (request.confirmedDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("A data confirmada não pode ser no passado");
        }
        excursion.setGroupTravelStatus("CONFIRMED");
        excursion.setGroupTravelConfirmedDate(request.confirmedDate());
        return ExcursionDto.from(excursionRepository.save(excursion));
    }

    @PostMapping("/{slug}/group-travel/reopen")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public ExcursionDto reopenGroupTravel(@AuthenticationPrincipal UserPrincipal principal, @PathVariable String slug) {
        Excursion excursion = find(slug);
        OwnershipGuard.requireOwnerOrAdmin(principal, excursion.getCreatedBy());
        excursion.setGroupTravelStatus(bookingRepository.existsByExcursion_Id(excursion.getId()) ? "OPEN" : "NONE");
        excursion.setGroupTravelConfirmedDate(null);
        return ExcursionDto.from(excursionRepository.save(excursion));
    }

    private Excursion find(String slug) {
        return excursionRepository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("Excursão não encontrada: " + slug));
    }
}
