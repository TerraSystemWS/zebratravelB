package cv.terrasystem.zebratravelb.hotel;

import java.time.LocalDate;
import java.util.List;

public record ReservationGuestDto(
        Integer id,
        String fullName,
        LocalDate dateOfBirth,
        String nationality,
        String passportNumber,
        boolean isPrimary,
        List<ReservationGuestDocumentDto> documents
) {
    public static ReservationGuestDto from(ReservationGuest guest) {
        return new ReservationGuestDto(
                guest.getId(),
                guest.getFullName(),
                guest.getDateOfBirth(),
                guest.getNationality(),
                guest.getPassportNumber(),
                guest.isPrimary(),
                guest.getDocuments().stream().map(ReservationGuestDocumentDto::from).toList()
        );
    }
}
