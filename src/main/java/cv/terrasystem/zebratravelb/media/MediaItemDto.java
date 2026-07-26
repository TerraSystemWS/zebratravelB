package cv.terrasystem.zebratravelb.media;

public record MediaItemDto(
        Integer id,
        Integer folderId,
        String name,
        String url,
        String contentType,
        Long sizeBytes
) {
    public static MediaItemDto from(MediaItem item, String baseUrl) {
        return new MediaItemDto(
                item.getId(),
                item.getFolder() != null ? item.getFolder().getId() : null,
                item.getOriginalFilename(),
                baseUrl + "/uploads/" + item.getStoredFilename(),
                item.getContentType(),
                item.getSizeBytes()
        );
    }
}
