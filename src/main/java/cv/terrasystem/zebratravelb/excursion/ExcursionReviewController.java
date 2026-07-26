package cv.terrasystem.zebratravelb.excursion;

import cv.terrasystem.zebratravelb.booking.BookingRepository;
import cv.terrasystem.zebratravelb.common.BadRequestException;
import cv.terrasystem.zebratravelb.common.NotFoundException;
import cv.terrasystem.zebratravelb.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/excursions/{slug}/reviews")
@RequiredArgsConstructor
public class ExcursionReviewController {

    private static final List<String> ELIGIBLE_STATUSES = List.of("CONFIRMED", "PAID");

    private final ExcursionReviewRepository reviewRepository;
    private final ExcursionRepository excursionRepository;
    private final BookingRepository bookingRepository;

    @GetMapping
    public List<ExcursionReviewDto> getReviews(@PathVariable String slug) {
        return reviewRepository.findByExcursion_SlugOrderByCreatedAtDesc(slug).stream().map(ExcursionReviewDto::from).toList();
    }

    @PostMapping
    public ExcursionReviewDto create(@AuthenticationPrincipal UserPrincipal principal, @PathVariable String slug, @RequestBody CreateExcursionReviewRequest request) {
        Excursion excursion = excursionRepository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("Excursão não encontrada: " + slug));

        if (request.rating() == null || request.rating() < 1 || request.rating() > 5) {
            throw new BadRequestException("A avaliação deve ser entre 1 e 5");
        }
        if (!bookingRepository.existsByExcursion_SlugAndUser_IdAndStatusIn(slug, principal.getId(), ELIGIBLE_STATUSES)) {
            throw new BadRequestException("Só podes avaliar excursões onde tenhas uma reserva confirmada ou paga");
        }
        if (reviewRepository.existsByExcursion_SlugAndUser_Id(slug, principal.getId())) {
            throw new BadRequestException("Já avaliaste esta excursão");
        }

        ExcursionReview review = new ExcursionReview();
        review.setExcursion(excursion);
        review.setUser(principal.getUser());
        review.setRating(request.rating());
        review.setComment(request.comment());
        return ExcursionReviewDto.from(reviewRepository.save(review));
    }
}
