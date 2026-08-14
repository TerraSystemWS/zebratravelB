package cv.terrasystem.zebratravelb.hotel;

import cv.terrasystem.zebratravelb.common.BadRequestException;
import cv.terrasystem.zebratravelb.common.NotFoundException;
import cv.terrasystem.zebratravelb.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

/**
 * Hóspedes associados a uma reserva de quarto (o hóspede principal e eventuais acompanhantes),
 * decorrelacionados de qualquer conta de cliente — ver dev-notes.md sobre "Hóspedes de reserva".
 * Podem ser geridos pelo cliente dono da reserva ou por ADMIN/AGENTE; as fotos de passaporte só
 * podem ser vistas/descarregadas por ADMIN/AGENTE.
 */
@RestController
@RequestMapping("/api/hotel/reservations/{reservationId}/guests")
@RequiredArgsConstructor
public class ReservationGuestController {

    private static final long MAX_DOCUMENT_SIZE_BYTES = 5L * 1024 * 1024;

    private final HotelReservationRepository reservationRepository;
    private final ReservationGuestRepository guestRepository;
    private final ReservationGuestDocumentRepository documentRepository;

    @Value("${app.documents.upload-dir}")
    private String documentsDir;

    @GetMapping
    public List<ReservationGuestDto> list(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Integer reservationId) {
        HotelReservation reservation = requireReservation(reservationId);
        requireAccess(principal, reservation);
        return guestRepository.findByReservation_IdOrderByCreatedAtAsc(reservationId).stream()
                .map(ReservationGuestDto::from).toList();
    }

    @PostMapping
    public ReservationGuestDto create(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Integer reservationId,
                                       @RequestBody GuestRequest request) {
        HotelReservation reservation = requireReservation(reservationId);
        requireAccess(principal, reservation);
        requireFullName(request);

        ReservationGuest guest = new ReservationGuest();
        guest.setReservation(reservation);
        applyRequest(guest, request);
        return ReservationGuestDto.from(guestRepository.save(guest));
    }

    @PatchMapping("/{guestId}")
    public ReservationGuestDto update(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Integer reservationId,
                                       @PathVariable Integer guestId, @RequestBody GuestRequest request) {
        HotelReservation reservation = requireReservation(reservationId);
        requireAccess(principal, reservation);
        requireFullName(request);

        ReservationGuest guest = requireGuest(reservationId, guestId);
        applyRequest(guest, request);
        return ReservationGuestDto.from(guestRepository.save(guest));
    }

    @DeleteMapping("/{guestId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Integer reservationId,
                                        @PathVariable Integer guestId) {
        HotelReservation reservation = requireReservation(reservationId);
        requireAccess(principal, reservation);
        ReservationGuest guest = requireGuest(reservationId, guestId);
        guest.getDocuments().forEach(doc -> deleteFileQuietly(doc.getStoredFilename()));
        guestRepository.delete(guest);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{guestId}/documents")
    public ReservationGuestDocumentDto uploadDocument(@AuthenticationPrincipal UserPrincipal principal,
                                                        @PathVariable Integer reservationId, @PathVariable Integer guestId,
                                                        @RequestParam("file") MultipartFile file) throws IOException {
        HotelReservation reservation = requireReservation(reservationId);
        requireAccess(principal, reservation);
        ReservationGuest guest = requireGuest(reservationId, guestId);

        if (file.isEmpty()) {
            throw new BadRequestException("O ficheiro é obrigatório");
        }
        if (file.getSize() > MAX_DOCUMENT_SIZE_BYTES) {
            throw new BadRequestException("O ficheiro não pode ter mais de 5MB");
        }
        String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "documento";
        String extension = extensionFor(file.getContentType(), original);
        if (extension == null) {
            throw new BadRequestException("O ficheiro tem de ser uma imagem (JPEG/PNG) ou PDF");
        }

        Path dir = Paths.get(documentsDir, "passports");
        Files.createDirectories(dir);
        String stored = UUID.randomUUID() + extension;
        Path target = dir.resolve(stored).normalize();
        if (!target.startsWith(dir.normalize())) {
            throw new BadRequestException("Nome de ficheiro inválido");
        }
        Files.write(target, file.getBytes());

        ReservationGuestDocument document = new ReservationGuestDocument();
        document.setGuest(guest);
        document.setStoredFilename(stored);
        document.setOriginalFilename(original);
        document.setContentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream");
        document.setSizeBytes(file.getSize());
        return ReservationGuestDocumentDto.from(documentRepository.save(document));
    }

    // Sensível (PII): só ADMIN/AGENTE, mesmo que o próprio cliente tenha enviado o documento —
    // decisão confirmada com o utilizador, ver plano.
    @GetMapping("/{guestId}/documents/{docId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Integer reservationId, @PathVariable Integer guestId,
                                                       @PathVariable Integer docId) {
        requireReservation(reservationId);
        requireGuest(reservationId, guestId);
        ReservationGuestDocument document = requireDocument(guestId, docId);
        Path path = Paths.get(documentsDir, "passports", document.getStoredFilename());
        if (!Files.exists(path)) {
            throw new NotFoundException("Ficheiro não encontrado");
        }
        Resource resource = new FileSystemResource(path);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(document.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(document.getOriginalFilename()).build().toString())
                .body(resource);
    }

    @DeleteMapping("/{guestId}/documents/{docId}")
    public ResponseEntity<Void> deleteDocument(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Integer reservationId,
                                                @PathVariable Integer guestId, @PathVariable Integer docId) {
        HotelReservation reservation = requireReservation(reservationId);
        requireAccess(principal, reservation);
        requireGuest(reservationId, guestId);
        ReservationGuestDocument document = requireDocument(guestId, docId);
        deleteFileQuietly(document.getStoredFilename());
        documentRepository.delete(document);
        return ResponseEntity.noContent().build();
    }

    private void requireFullName(GuestRequest request) {
        if (request.fullName() == null || request.fullName().isBlank()) {
            throw new BadRequestException("Nome do hóspede é obrigatório");
        }
    }

    private void applyRequest(ReservationGuest guest, GuestRequest request) {
        guest.setFullName(request.fullName());
        guest.setDateOfBirth(request.dateOfBirth());
        guest.setNationality(request.nationality());
        guest.setPassportNumber(request.passportNumber());
        guest.setPrimary(Boolean.TRUE.equals(request.isPrimary()));
    }

    private void deleteFileQuietly(String storedFilename) {
        try {
            Files.deleteIfExists(Paths.get(documentsDir, "passports", storedFilename));
        } catch (IOException ignored) {
            // se o ficheiro já não existir no disco, continua a apagar o registo na BD
        }
    }

    private String extensionFor(String contentType, String originalFilename) {
        String lower = originalFilename.toLowerCase();
        if ("image/jpeg".equals(contentType) || lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return ".jpg";
        if ("image/png".equals(contentType) || lower.endsWith(".png")) return ".png";
        if ("application/pdf".equals(contentType) || lower.endsWith(".pdf")) return ".pdf";
        return null;
    }

    private HotelReservation requireReservation(Integer reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("Reserva não encontrada: " + reservationId));
    }

    private ReservationGuest requireGuest(Integer reservationId, Integer guestId) {
        ReservationGuest guest = guestRepository.findById(guestId)
                .orElseThrow(() -> new NotFoundException("Hóspede não encontrado: " + guestId));
        if (!guest.getReservation().getId().equals(reservationId)) {
            throw new NotFoundException("Hóspede não encontrado: " + guestId);
        }
        return guest;
    }

    private ReservationGuestDocument requireDocument(Integer guestId, Integer docId) {
        ReservationGuestDocument document = documentRepository.findById(docId)
                .orElseThrow(() -> new NotFoundException("Documento não encontrado: " + docId));
        if (!document.getGuest().getId().equals(guestId)) {
            throw new NotFoundException("Documento não encontrado: " + docId);
        }
        return document;
    }

    private boolean isStaff(UserPrincipal principal) {
        return principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_AGENTE"));
    }

    private void requireAccess(UserPrincipal principal, HotelReservation reservation) {
        if (isStaff(principal)) return;
        if (reservation.getUser() != null && reservation.getUser().getId().equals(principal.getId())) return;
        throw new AccessDeniedException("Sem permissão para gerir os hóspedes desta reserva");
    }
}
