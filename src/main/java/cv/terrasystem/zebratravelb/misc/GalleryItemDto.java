package cv.terrasystem.zebratravelb.misc;

import java.util.List;
import java.util.stream.Collectors;

public record GalleryItemDto(Integer id, String imgSrc, List<String> categories) {
    public static GalleryItemDto from(GalleryItem item) {
        return new GalleryItemDto(
                item.getId(),
                item.getImgSrc(),
                item.getCategories().stream().map(GalleryCategory::getName).collect(Collectors.toList())
        );
    }
}
