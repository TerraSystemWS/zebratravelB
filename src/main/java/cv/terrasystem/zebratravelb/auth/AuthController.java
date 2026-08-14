package cv.terrasystem.zebratravelb.auth;

import cv.terrasystem.zebratravelb.security.UserPrincipal;
import cv.terrasystem.zebratravelb.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public AuthResponse me(@AuthenticationPrincipal UserPrincipal principal) {
        User user = principal.getUser();
        return new AuthResponse(null, user.getId(), user.getFullName(), user.getEmail(), user.getRole().getName(), user.getPhone());
    }

    @PatchMapping("/me")
    public AuthResponse updateMe(@AuthenticationPrincipal UserPrincipal principal, @RequestBody UpdateMeRequest request) {
        return authService.updateMe(principal.getUser(), request);
    }
}
