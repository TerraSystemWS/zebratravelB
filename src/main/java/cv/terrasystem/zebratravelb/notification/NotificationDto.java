package cv.terrasystem.zebratravelb.notification;

import java.time.LocalDateTime;

public record NotificationDto(
        Long id,
        String type,
        String title,
        String body,
        String linkUrl,
        Integer relatedEntityId,
        boolean read,
        LocalDateTime createdAt
) {
    public static NotificationDto from(Notification n) {
        return new NotificationDto(
                n.getId(), n.getType(), n.getTitle(), n.getBody(), n.getLinkUrl(),
                n.getRelatedEntityId(), n.isRead(), n.getCreatedAt()
        );
    }
}
