package cv.terrasystem.zebratravelb.misc;

import cv.terrasystem.zebratravelb.common.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/travel-packages")
@RequiredArgsConstructor
public class TravelPackageController {

    private final TravelPackageRepository repository;

    @GetMapping
    public List<TravelPackage> getAll() {
        return repository.findAll();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public TravelPackage create(@RequestBody TravelPackage pkg) {
        pkg.setId(null);
        return repository.save(pkg);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public TravelPackage update(@PathVariable Integer id, @RequestBody TravelPackage pkg) {
        TravelPackage existing = repository.findById(id).orElseThrow(() -> new NotFoundException("Pacote não encontrado: " + id));
        existing.setTitle(pkg.getTitle());
        existing.setDuration(pkg.getDuration());
        existing.setLocation(pkg.getLocation());
        existing.setPrice(pkg.getPrice());
        existing.setDescription(pkg.getDescription());
        existing.setLink(pkg.getLink());
        existing.setImageUrl(pkg.getImageUrl());
        return repository.save(existing);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Pacote não encontrado: " + id);
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
