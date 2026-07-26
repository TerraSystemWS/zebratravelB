package cv.terrasystem.zebratravelb.excursion;

import java.time.LocalDateTime;

public record ExcursionReviewDto(
        Integer id,
        String excursionSlug,
        String userName,
        Integer rating,
        String comment,
        LocalDateTime createdAt
) {
    public static ExcursionReviewDto from(ExcursionReview review) {
        String name = review.getUser().getFullName() != null
                ? review.getUser().getFullName()
                : review.getUser().getUsername();
        return new ExcursionReviewDto(
                review.getId(),
                review.getExcursion().getSlug(),
                name,
                review.getRating(),
                review.getComment(),
                review.getCreatedAt()
        );
    }
}
