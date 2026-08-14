package cv.terrasystem.zebratravelb.auth;

public record AuthResponse(
        String token,
        Integer id,
        String fullName,
        String email,
        String role,
        String phone
) {
}
