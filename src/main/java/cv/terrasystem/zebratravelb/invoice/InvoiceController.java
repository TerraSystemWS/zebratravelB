package cv.terrasystem.zebratravelb.invoice;

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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceRepository invoiceRepository;

    @Value("${app.documents.upload-dir}")
    private String documentsDir;

    @GetMapping("/mine")
    public List<InvoiceDto> getMine(@AuthenticationPrincipal UserPrincipal principal) {
        return invoiceRepository.findByUser_IdOrderByCreatedAtDesc(principal.getId()).stream()
                .map(InvoiceDto::from).toList();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public List<InvoiceDto> getAll() {
        return invoiceRepository.findAllByOrderByCreatedAtDesc().stream().map(InvoiceDto::from).toList();
    }

    // Origem: cliente dono da fatura, ou ADMIN/AGENTE. Mesmo padrão de acesso já usado nos
    // documentos de hóspede (ReservationGuestDocumentController).
    @GetMapping("/{id}/pdf")
    public ResponseEntity<Resource> downloadPdf(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Integer id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Fatura não encontrada: " + id));

        boolean isStaff = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_AGENTE"));
        boolean isOwner = invoice.getUser() != null && invoice.getUser().getId().equals(principal.getId());
        if (!isStaff && !isOwner) {
            throw new NotFoundException("Fatura não encontrada: " + id);
        }
        if (invoice.getPdfStoredFilename() == null) {
            throw new NotFoundException("O PDF desta fatura ainda não está disponível");
        }

        Path path = Paths.get(documentsDir, "invoices", invoice.getPdfStoredFilename());
        Resource resource = new FileSystemResource(path);
        if (!resource.exists()) {
            throw new NotFoundException("Ficheiro da fatura não encontrado");
        }

        String filename = invoice.documentNumber().replace("/", "-") + ".pdf";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename(filename).build().toString())
                .body(resource);
    }
}
