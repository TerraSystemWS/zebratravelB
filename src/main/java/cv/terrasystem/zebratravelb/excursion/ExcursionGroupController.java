package cv.terrasystem.zebratravelb.excursion;

import cv.terrasystem.zebratravelb.common.BadRequestException;
import cv.terrasystem.zebratravelb.common.NotFoundException;
import cv.terrasystem.zebratravelb.common.OwnershipGuard;
import cv.terrasystem.zebratravelb.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

// Gestão dos grupos de viagem (cada excursão pode ter vários ao longo do
// tempo — ver BookingController.create, que abre um grupo novo sempre que
// já não há nenhum OPEN para a excursão). A listagem pública dos grupos
// confirmados continua em ExcursionController (GET /api/excursions/group-travel).
@RestController
@RequestMapping("/api/excursion-groups")
@RequiredArgsConstructor
public class ExcursionGroupController {

    private final ExcursionGroupRepository excursionGroupRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public List<ExcursionGroupDto> getAll() {
        return excursionGroupRepository.findAll().stream().map(ExcursionGroupDto::from).toList();
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
