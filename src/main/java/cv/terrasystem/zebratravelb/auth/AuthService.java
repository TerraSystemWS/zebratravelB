package cv.terrasystem.zebratravelb.auth;

import cv.terrasystem.zebratravelb.common.BadRequestException;
import cv.terrasystem.zebratravelb.security.JwtService;
import cv.terrasystem.zebratravelb.security.UserPrincipal;
import cv.terrasystem.zebratravelb.subscriber.SubscriberService;
import cv.terrasystem.zebratravelb.user.Role;
import cv.terrasystem.zebratravelb.user.RoleRepository;
import cv.terrasystem.zebratravelb.user.User;
import cv.terrasystem.zebratravelb.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final SubscriberService subscriberService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Este email já está registado");
        }

        Role clientRole = roleRepository.findByName(Role.CLIENT)
                .orElseThrow(() -> new IllegalStateException("Role CLIENT não existe"));

        User user = new User();
        user.setUsername(request.email());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName());
        user.setPhone(request.phone());
        user.setRole(clientRole);
        user.setStatus("ACTIVE");
        userRepository.save(user);
        subscriberService.subscribeIfAbsent(user.getEmail());

        return buildResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadRequestException("Credenciais inválidas"));

        if ("ANONYMIZED".equals(user.getStatus())) {
            throw new BadRequestException("Esta conta foi removida");
        }

        return buildResponse(user);
    }

    // Auto-serviço: qualquer utilizador autenticado (ADMIN/AGENTE/CLIENT) pode editar os
    // seus próprios dados — password atual sempre exigida antes de qualquer alteração,
    // mesmo só para mudar o nome, para confirmar identidade (mesma cautela que um "muda
    // password" normal, aplicada a toda a edição da própria conta). Devolve um token novo
    // porque o token embebe o email (subject) — um token antigo deixaria de bater certo
    // com o email novo no próximo pedido.
    @Transactional
    public AuthResponse updateMe(User user, UpdateMeRequest request) {
        if (request.currentPassword() == null || !passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Password atual incorreta");
        }
        if (request.fullName() == null || request.fullName().isBlank()) {
            throw new BadRequestException("Nome é obrigatório");
        }
        if (request.email() == null || request.email().isBlank()) {
            throw new BadRequestException("Email é obrigatório");
        }
        if (!request.email().equalsIgnoreCase(user.getEmail()) && userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Este email já está registado");
        }

        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setUsername(request.email());
        user.setPhone(request.phone());

        if (request.newPassword() != null && !request.newPassword().isBlank()) {
            if (request.newPassword().length() < 6) {
                throw new BadRequestException("A nova password deve ter pelo menos 6 caracteres");
            }
            user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        }

        userRepository.save(user);
        return buildResponse(user);
    }

    private AuthResponse buildResponse(User user) {
        String token = jwtService.generateToken(new UserPrincipal(user));
        return new AuthResponse(token, user.getId(), user.getFullName(), user.getEmail(), user.getRole().getName(), user.getPhone());
    }
}
