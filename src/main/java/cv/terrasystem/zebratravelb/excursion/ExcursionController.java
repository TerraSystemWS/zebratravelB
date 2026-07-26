package cv.terrasystem.zebratravelb.excursion;

import cv.terrasystem.zebratravelb.common.NotFoundException;
import cv.terrasystem.zebratravelb.common.OwnershipGuard;
import cv.terrasystem.zebratravelb.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/excursions")
@RequiredArgsConstructor
public class ExcursionController {

    private final ExcursionRepository excursionRepository;

    @GetMapping
    public List<ExcursionDto> getAll() {
        return excursionRepository.findAll().stream().map(ExcursionDto::from).toList();
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
        excursionRepository.delete(excursion);
        return ResponseEntity.noContent().build();
    }

    private Excursion find(String slug) {
        return excursionRepository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("Excursão não encontrada: " + slug));
    }
}
