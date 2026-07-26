package cv.terrasystem.zebratravelb.hotel;

import java.time.LocalDateTime;

public record ReviewDto(
        Integer id,
        Integer roomId,
        String userName,
        Integer rating,
        String comment,
        LocalDateTime createdAt
) {
    public static ReviewDto from(HotelRoomReview review) {
        String name = review.getUser().getFullName() != null
                ? review.getUser().getFullName()
                : review.getUser().getUsername();
        return new ReviewDto(review.getId(), review.getRoom().getId(), name, review.getRating(), review.getComment(), review.getCreatedAt());
    }
}
