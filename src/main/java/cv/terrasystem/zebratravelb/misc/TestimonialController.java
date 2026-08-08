package cv.terrasystem.zebratravelb.misc;

import cv.terrasystem.zebratravelb.common.BadRequestException;
import cv.terrasystem.zebratravelb.common.NotFoundException;
import cv.terrasystem.zebratravelb.excursion.ExcursionReview;
import cv.terrasystem.zebratravelb.excursion.ExcursionReviewRepository;
import cv.terrasystem.zebratravelb.hotel.HotelRoomReview;
import cv.terrasystem.zebratravelb.hotel.HotelRoomReviewRepository;
import cv.terrasystem.zebratravelb.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/testimonials")
@RequiredArgsConstructor
public class TestimonialController {

    private final TestimonialRepository repository;
    private final ExcursionReviewRepository excursionReviewRepository;
    private final HotelRoomReviewRepository hotelRoomReviewRepository;

    @GetMapping
    public List<Testimonial> getAll() {
        return repository.findAll();
    }

    @PostMapping("/from-review")
    public Testimonial createFromReview(@AuthenticationPrincipal UserPrincipal principal, @RequestBody TestimonialFromReviewRequest request) {
        String type = request.sourceType() != null ? request.sourceType().toUpperCase() : null;
        if (!"EXCURSION".equals(type) && !"HOTEL_ROOM".equals(type)) {
            throw new BadRequestException("Tipo de origem inválido: " + request.sourceType());
        }
        if (request.sourceReviewId() == null) {
            throw new BadRequestException("sourceReviewId é obrigatório");
        }
        if (repository.existsBySourceReviewTypeAndSourceReviewId(type, request.sourceReviewId())) {
            throw new BadRequestException("Já criaste um testemunho a partir desta avaliação");
        }

        String name;
        String text;
        Integer rating;

        if ("EXCURSION".equals(type)) {
            ExcursionReview review = excursionReviewRepository.findById(request.sourceReviewId())
                    .orElseThrow(() -> new NotFoundException("Avaliação não encontrada: " + request.sourceReviewId()));
            if (!review.getUser().getId().equals(principal.getId())) {
                throw new AccessDeniedException("Só podes transformar a tua própria avaliação num testemunho");
            }
            name = review.getUser().getFullName() != null ? review.getUser().getFullName() : review.getUser().getUsername();
            text = review.getComment();
            rating = review.getRating();
            review.setTestimonial(true);
            excursionReviewRepository.save(review);
        } else {
            HotelRoomReview review = hotelRoomReviewRepository.findById(request.sourceReviewId())
                    .orElseThrow(() -> new NotFoundException("Avaliação não encontrada: " + request.sourceReviewId()));
            if (!review.getUser().getId().equals(principal.getId())) {
                throw new AccessDeniedException("Só podes transformar a tua própria avaliação num testemunho");
            }
            name = review.getUser().getFullName() != null ? review.getUser().getFullName() : review.getUser().getUsername();
            text = review.getComment();
            rating = review.getRating();
            review.setTestimonial(true);
            hotelRoomReviewRepository.save(review);
        }

        Testimonial testimonial = new Testimonial();
        testimonial.setName(name);
        testimonial.setText(text != null ? text : "");
        testimonial.setRating(BigDecimal.valueOf(rating));
        testimonial.setDesignation(request.designation());
        testimonial.setSourceReviewType(type);
        testimonial.setSourceReviewId(request.sourceReviewId());
        return repository.save(testimonial);
    }

    // Não há criação manual (nem para ADMIN nem AGENTE) — um testemunho só nasce de uma
    // review real e paga através de /from-review acima. Editar/apagar continuam possíveis
    // para moderação dos testemunhos já existentes.
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
