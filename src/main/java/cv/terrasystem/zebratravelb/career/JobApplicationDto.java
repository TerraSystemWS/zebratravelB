package cv.terrasystem.zebratravelb.career;

import java.time.LocalDateTime;

public record JobApplicationDto(
        Integer id,
        String name,
        String phone,
        String email,
        String area,
        String description,
        String cvOriginalFilename,
        LocalDateTime createdAt
) {
    public static JobApplicationDto from(JobApplication a) {
        return new JobApplicationDto(
                a.getId(), a.getName(), a.getPhone(), a.getEmail(), a.getArea(),
                a.getDescription(), a.getCvOriginalFilename(), a.getCreatedAt()
        );
    }
}
