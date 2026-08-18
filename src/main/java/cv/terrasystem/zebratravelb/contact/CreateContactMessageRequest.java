package cv.terrasystem.zebratravelb.contact;

public record CreateContactMessageRequest(
        String name,
        String email,
        String phone,
        String subject,
        String message,
        String turnstileToken
) {
}
