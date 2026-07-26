package cv.terrasystem.zebratravelb.tour;

import cv.terrasystem.zebratravelb.common.NotFoundException;
import cv.terrasystem.zebratravelb.common.OwnershipGuard;
import cv.terrasystem.zebratravelb.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/tours")
@RequiredArgsConstructor
public class TourController {

    private final TourRepository tourRepository;
    private final CategoryRepository categoryRepository;

    @GetMapping
    public List<TourDto> getAll() {
        return tourRepository.findAll().stream().map(TourDto::from).toList();
    }

    @GetMapping("/{id}")
    public TourDto getById(@PathVariable Integer id) {
        return TourDto.from(find(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public TourDto create(@AuthenticationPrincipal UserPrincipal principal, @RequestBody TourDto dto) {
        Tour tour = new Tour();
        dto.applyTo(tour);
        tour.setCategories(resolveCategories(dto.category()));
        tour.setCreatedBy(principal.getUser());
        return TourDto.from(tourRepository.save(tour));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public TourDto update(@PathVariable Integer id, @RequestBody TourDto dto) {
        Tour tour = find(id);
        dto.applyTo(tour);
        if (dto.category() != null) {
            tour.setCategories(resolveCategories(dto.category()));
        }
        return TourDto.from(tourRepository.save(tour));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Integer id) {
        Tour tour = find(id);
        OwnershipGuard.requireOwnerOrAdmin(principal, tour.getCreatedBy());
        tourRepository.delete(tour);
        return ResponseEntity.noContent().build();
    }

    private Tour find(Integer id) {
        return tourRepository.findById(id).orElseThrow(() -> new NotFoundException("Tour não encontrado: " + id));
    }

    private Set<Category> resolveCategories(List<String> names) {
        if (names == null) return new HashSet<>();
        Set<Category> categories = new HashSet<>();
        for (String name : names) {
            Category category = categoryRepository.findByName(name)
                    .orElseGet(() -> {
                        Category c = new Category();
                        c.setName(name);
                        return categoryRepository.save(c);
                    });
            categories.add(category);
        }
        return categories;
    }
}
