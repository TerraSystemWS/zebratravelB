package cv.terrasystem.zebratravelb.hotel;

import java.time.LocalDate;

public record GuestRequest(
        String fullName,
        LocalDate dateOfBirth,
        String nationality,
        String passportNumber,
        Boolean isPrimary
) {
}
