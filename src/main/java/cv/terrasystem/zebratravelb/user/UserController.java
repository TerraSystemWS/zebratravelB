package cv.terrasystem.zebratravelb.user;

import cv.terrasystem.zebratravelb.common.BadRequestException;
import cv.terrasystem.zebratravelb.common.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public List<UserDto> getAll() {
        return userRepository.findAll().stream().map(UserDto::from).toList();
    }

    @PostMapping
    public UserDto create(@RequestBody UserUpsertDto dto) {
        if (dto.email() == null || dto.email().isBlank()) {
            throw new BadRequestException("Email é obrigatório");
        }
        if (userRepository.existsByEmail(dto.email())) {
            throw new BadRequestException("Este email já está registado");
        }
        if (dto.password() == null || dto.password().length() < 6) {
            throw new BadRequestException("Senha deve ter pelo menos 6 caracteres");
        }

        User user = new User();
        user.setUsername(dto.email());
        user.setEmail(dto.email());
        user.setFullName(dto.name());
        user.setPasswordHash(passwordEncoder.encode(dto.password()));
        user.setRole(resolveRole(dto.role()));
        user.setStatus(dto.status() != null ? dto.status() : "ACTIVE");
        return UserDto.from(userRepository.save(user));
    }

    @PutMapping("/{id}")
    public UserDto update(@PathVariable Integer id, @RequestBody UserUpsertDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Utilizador não encontrado: " + id));
        if (dto.name() != null) user.setFullName(dto.name());
        if (dto.role() != null) user.setRole(resolveRole(dto.role()));
        if (dto.status() != null) user.setStatus(dto.status());
        if (dto.password() != null && !dto.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(dto.password()));
        }
        return UserDto.from(userRepository.save(user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("User not found: " + id);
        }
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private Role resolveRole(String name) {
        String roleName = (name != null ? name : Role.CLIENT).toUpperCase();
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new BadRequestException("Role inválida: " + roleName));
    }
}
