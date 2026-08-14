package cv.terrasystem.zebratravelb.career;

import cv.terrasystem.zebratravelb.common.BadRequestException;
import cv.terrasystem.zebratravelb.common.NotFoundException;
import cv.terrasystem.zebratravelb.notification.Notification;
import cv.terrasystem.zebratravelb.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/job-applications")
@RequiredArgsConstructor
public class JobApplicationController {

    private static final long MAX_CV_SIZE_BYTES = 2L * 1024 * 1024;

    private final JobApplicationRepository repository;
    private final NotificationService notificationService;

    @Value("${app.media.upload-dir}")
    private String uploadDir;

    @PostMapping(consumes = "multipart/form-data")
    public void apply(
            @RequestParam String name,
            @RequestParam String phone,
            @RequestParam String email,
            @RequestParam String area,
            @RequestParam String description,
            @RequestParam("cv") MultipartFile cv
    ) throws IOException {
        if (name.isBlank() || phone.isBlank() || email.isBlank() || description.isBlank()) {
            throw new BadRequestException("Todos os campos são obrigatórios");
        }
        if (!JobArea.VALID.contains(area)) {
            throw new BadRequestException("Área inválida: " + area);
        }
        if (cv.isEmpty()) {
            throw new BadRequestException("O CV é obrigatório");
        }
        if (cv.getSize() > MAX_CV_SIZE_BYTES) {
            throw new BadRequestException("O CV não pode ter mais de 2MB");
        }
        String original = cv.getOriginalFilename() != null ? cv.getOriginalFilename() : "cv.pdf";
        boolean isPdf = "application/pdf".equals(cv.getContentType()) || original.toLowerCase().endsWith(".pdf");
        if (!isPdf) {
            throw new BadRequestException("O CV tem de ser um ficheiro PDF");
        }

        Path dir = Paths.get(uploadDir, "cv");
        Files.createDirectories(dir);

        String stored = UUID.randomUUID() + ".pdf";
        Path target = dir.resolve(stored).normalize();
        if (!target.startsWith(dir.normalize())) {
            throw new BadRequestException("Nome de ficheiro inválido");
        }
        Files.write(target, cv.getBytes());

        JobApplication application = new JobApplication();
        application.setName(name);
        application.setPhone(phone);
        application.setEmail(email);
        application.setArea(area);
        application.setDescription(description);
        application.setCvStoredFilename(stored);
        application.setCvOriginalFilename(original);
        JobApplication saved = repository.save(application);
        notificationService.notify(Notification.JOB_APPLICATION, "Nova candidatura de Carreiras",
                name + " — " + area, "/dashboard/carreiras", saved.getId());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public List<JobApplicationDto> getAll() {
        return repository.findAll().stream().map(JobApplicationDto::from).toList();
    }

    @GetMapping("/{id}/cv")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public ResponseEntity<Resource> getCv(@PathVariable Integer id) {
        JobApplication application = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Candidatura não encontrada: " + id));
        Path path = Paths.get(uploadDir, "cv", application.getCvStoredFilename());
        if (!Files.exists(path)) {
            throw new NotFoundException("Ficheiro do CV não encontrado");
        }
        Resource resource = new FileSystemResource(path);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(application.getCvOriginalFilename()).build().toString())
                .body(resource);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        JobApplication application = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Candidatura não encontrada: " + id));
        try {
            Files.deleteIfExists(Paths.get(uploadDir, "cv", application.getCvStoredFilename()));
        } catch (IOException ignored) {
            // if the file is already gone, still remove the DB record
        }
        repository.delete(application);
        return ResponseEntity.noContent().build();
    }
}
