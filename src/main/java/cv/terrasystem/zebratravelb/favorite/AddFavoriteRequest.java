package cv.terrasystem.zebratravelb.favorite;

public record AddFavoriteRequest(
        String itemType,
        Integer itemId
) {
}
