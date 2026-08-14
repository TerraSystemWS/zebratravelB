package cv.terrasystem.zebratravelb.auth;

public record UpdateMeRequest(
        String fullName,
        String email,
        String phone,
        String currentPassword,
        String newPassword
) {
}
