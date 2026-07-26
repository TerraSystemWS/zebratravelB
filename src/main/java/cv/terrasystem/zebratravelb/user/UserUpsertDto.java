package cv.terrasystem.zebratravelb.user;

public record UserUpsertDto(
        String name,
        String email,
        String password,
        String role,
        String status
) {
}
