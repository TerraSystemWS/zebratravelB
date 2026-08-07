package cv.terrasystem.zebratravelb.user;

import cv.terrasystem.zebratravelb.booking.BookingRepository;
import cv.terrasystem.zebratravelb.common.BadRequestException;
import cv.terrasystem.zebratravelb.common.NotFoundException;
import cv.terrasystem.zebratravelb.excursion.ExcursionReviewRepository;
import cv.terrasystem.zebratravelb.hotel.HotelReservationRepository;
import cv.terrasystem.zebratravelb.hotel.HotelRoomReviewRepository;
import cv.terrasystem.zebratravelb.order.OrderRepository;
import cv.terrasystem.zebratravelb.post.AuthorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final BookingRepository bookingRepository;
    private final HotelReservationRepository hotelReservationRepository;
    private final OrderRepository orderRepository;
    private final ExcursionReviewRepository excursionReviewRepository;
    private final HotelRoomReviewRepository hotelRoomReviewRepository;
    private final AuthorRepository authorRepository;

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
    public DeleteUserResponse delete(@PathVariable Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));

        boolean hasDependents = bookingRepository.existsByUserId(id)
                || hotelReservationRepository.existsByUser_Id(id)
                || orderRepository.existsByUser_Id(id)
                || excursionReviewRepository.existsByUser_Id(id)
                || hotelRoomReviewRepository.existsByUser_Id(id)
                || authorRepository.existsByUser_Id(id);

        if (!hasDependents) {
            userRepository.deleteById(id);
            return new DeleteUserResponse(true);
        }

        user.setUsername("deleted-user-" + id);
        user.setEmail("deleted-" + id + "@zebratravel.invalid");
        user.setFullName("Utilizador removido");
        user.setPhone(null);
        user.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setStatus("ANONYMIZED");
        userRepository.save(user);
        return new DeleteUserResponse(false);
    }

    private Role resolveRole(String name) {
        String roleName = (name != null ? name : Role.CLIENT).toUpperCase();
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new BadRequestException("Role inválida: " + roleName));
    }
}
