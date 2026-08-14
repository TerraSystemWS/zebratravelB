package cv.terrasystem.zebratravelb.hotel;

import java.time.LocalDateTime;

public record ReservationGuestDocumentDto(
        Integer id,
        String originalFilename,
        String contentType,
        Long sizeBytes,
        LocalDateTime uploadedAt
) {
    public static ReservationGuestDocumentDto from(ReservationGuestDocument doc) {
        return new ReservationGuestDocumentDto(
                doc.getId(),
                doc.getOriginalFilename(),
                doc.getContentType(),
                doc.getSizeBytes(),
                doc.getUploadedAt()
        );
    }
}
