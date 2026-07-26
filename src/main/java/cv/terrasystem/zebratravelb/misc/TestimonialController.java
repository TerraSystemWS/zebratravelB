package cv.terrasystem.zebratravelb.misc;

import cv.terrasystem.zebratravelb.common.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/testimonials")
@RequiredArgsConstructor
public class TestimonialController {

    private final TestimonialRepository repository;

    @GetMapping
    public List<Testimonial> getAll() {
        return repository.findAll();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Testimonial create(@RequestBody Testimonial testimonial) {
        testimonial.setId(null);
        return repository.save(testimonial);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Testimonial update(@PathVariable Integer id, @RequestBody Testimonial testimonial) {
        Testimonial existing = repository.findById(id).orElseThrow(() -> new NotFoundException("Testemunho não encontrado: " + id));
        existing.setImage(testimonial.getImage());
        existing.setText(testimonial.getText());
        existing.setName(testimonial.getName());
        existing.setDesignation(testimonial.getDesignation());
        existing.setRating(testimonial.getRating());
        existing.setBackgroundImage(testimonial.getBackgroundImage());
        existing.setLink(testimonial.getLink());
        return repository.save(existing);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Testemunho não encontrado: " + id);
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
