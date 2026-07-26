package cv.terrasystem.zebratravelb.misc;

import cv.terrasystem.zebratravelb.common.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sponsors")
@RequiredArgsConstructor
public class SponsorController {

    private final SponsorRepository repository;

    @GetMapping
    public List<Sponsor> getAll() {
        return repository.findAll();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Sponsor create(@RequestBody Sponsor sponsor) {
        sponsor.setId(null);
        return repository.save(sponsor);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Sponsor update(@PathVariable Integer id, @RequestBody Sponsor sponsor) {
        Sponsor existing = repository.findById(id).orElseThrow(() -> new NotFoundException("Sponsor não encontrado: " + id));
        existing.setImage(sponsor.getImage());
        existing.setLink(sponsor.getLink());
        return repository.save(existing);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Sponsor não encontrado: " + id);
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
