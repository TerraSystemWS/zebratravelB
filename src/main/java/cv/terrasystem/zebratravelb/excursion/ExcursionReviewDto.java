package cv.terrasystem.zebratravelb.excursion;

import java.time.LocalDateTime;

public record ExcursionReviewDto(
        Integer id,
        String excursionSlug,
        Integer userId,
        String userName,
        Integer rating,
        String comment,
        LocalDateTime createdAt,
        boolean isTestimonial
) {
    public static ExcursionReviewDto from(ExcursionReview review) {
        String name = review.getUser().getFullName() != null
                ? review.getUser().getFullName()
                : review.getUser().getUsername();
        return new ExcursionReviewDto(
                review.getId(),
                review.getExcursion().getSlug(),
                review.getUser().getId(),
                name,
                review.getRating(),
                review.getComment(),
                review.getCreatedAt(),
                review.isTestimonial()
        );
    }
}
