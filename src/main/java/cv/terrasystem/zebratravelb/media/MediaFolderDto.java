package cv.terrasystem.zebratravelb.media;

public record MediaFolderDto(Integer id, String name, Integer parentId) {
    public static MediaFolderDto from(MediaFolder folder) {
        return new MediaFolderDto(
                folder.getId(),
                folder.getName(),
                folder.getParent() != null ? folder.getParent().getId() : null
        );
    }
}
