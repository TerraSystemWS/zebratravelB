package cv.terrasystem.zebratravelb.contact;

import java.time.LocalDateTime;

public record ContactMessageDto(
        Integer id,
        String name,
        String email,
        String phone,
        String subject,
        String message,
        boolean read,
        LocalDateTime createdAt
) {
    public static ContactMessageDto from(ContactMessage m) {
        return new ContactMessageDto(
                m.getId(), m.getName(), m.getEmail(), m.getPhone(), m.getSubject(),
                m.getMessage(), m.isRead(), m.getCreatedAt()
        );
    }
}
