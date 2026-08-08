package cv.terrasystem.zebratravelb.excursion;

import java.math.BigDecimal;
import java.util.List;

public record ExcursionDto(
        Integer id,
        String slug,
        String title,
        String image,
        BigDecimal price,
        String duration,
        String location,
        BigDecimal rating,
        Integer reviews,
        String description,
        List<String> categories,
        Integer createdById,
        String status
) {
    public static ExcursionDto from(Excursion e) {
        return new ExcursionDto(
                e.getId(), e.getSlug(), e.getTitle(), e.getImage(), e.getPrice(),
                e.getDuration(), e.getLocation(), e.getRating(), e.getReviews(),
                e.getDescription(), e.getCategories() != null ? List.of(e.getCategories()) : List.of(),
                e.getCreatedBy() != null ? e.getCreatedBy().getId() : null,
                e.getStatus()
        );
    }

    // rating/reviews are aggregated from real ExcursionReview submissions
    // (see ExcursionReviewController) and must not be settable from here.
    // status also isn't settable here — only through /archive and /restore.
    // Group-travel state lives on ExcursionGroup now (see ExcursionGroupController).
    public void applyTo(Excursion e) {
        e.setSlug(slug);
        e.setTitle(title);
        e.setImage(image);
        e.setPrice(price);
        e.setDuration(duration);
        e.setLocation(location);
        e.setDescription(description);
        e.setCategories(categories != null ? categories.toArray(new String[0]) : new String[0]);
    }
}
