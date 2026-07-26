package cv.terrasystem.zebratravelb.post;

import java.time.LocalDate;

public record PostDto(
        Integer id,
        String title,
        String author,
        LocalDate date,
        String image,
        String content,
        String category,
        String description,
        String slug,
        Integer createdById
) {
    public static PostDto from(Post p) {
        return new PostDto(
                p.getId(), p.getTitle(),
                p.getAuthor() != null ? p.getAuthor().getName() : null,
                p.getDate(), p.getImage(), p.getContent(),
                p.getCategory() != null ? p.getCategory().getName() : null,
                p.getDescription(), p.getSlug(),
                p.getCreatedBy() != null ? p.getCreatedBy().getId() : null
        );
    }
}
