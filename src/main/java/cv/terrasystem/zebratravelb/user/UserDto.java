package cv.terrasystem.zebratravelb.user;

public record UserDto(
        Integer id,
        String name,
        String email,
        String role,
        String status
) {
    public static UserDto from(User user) {
        return new UserDto(
                user.getId(),
                user.getFullName() != null ? user.getFullName() : user.getUsername(),
                user.getEmail(),
                user.getRole().getName(),
                user.getStatus()
        );
    }
}
