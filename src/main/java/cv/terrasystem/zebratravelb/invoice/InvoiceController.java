package cv.terrasystem.zebratravelb.invoice;

import cv.terrasystem.zebratravelb.common.BadRequestException;
import cv.terrasystem.zebratravelb.common.NotFoundException;
import cv.terrasystem.zebratravelb.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    // Só formatos que o OpenPDF consegue embutir diretamente num PDF sem conversão nenhuma
    // (nada de WebP aqui — ao contrário do resto da Media Library, este logótipo é lido pelo
    // gerador de PDF no servidor, não só mostrado num browser).
    private static final Set<String> LOGO_CONTENT_TYPES = Set.of("image/png", "image/jpeg", "image/jpg");
    private static final String LOGO_SUBFOLDER = "invoice-logo";

    private final InvoiceRepository invoiceRepository;
    private final InvoiceCompanyProfileService companyProfileService;
    private final InvoiceSigningService signingService;

    @Value("${app.documents.upload-dir}")
    private String documentsDir;

    @Value("${app.media.upload-dir}")
    private String mediaUploadDir;

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

    // ---- Configuração da empresa para o cabeçalho das faturas (Contas > Configuração) --------

    @GetMapping("/company-profile")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public InvoiceCompanyProfileDto getCompanyProfile(HttpServletRequest request) {
        return InvoiceCompanyProfileDto.from(companyProfileService.get(), baseUrl(request));
    }

    @PutMapping("/company-profile")
    @PreAuthorize("hasRole('ADMIN')")
    public InvoiceCompanyProfileDto updateCompanyProfile(@RequestBody UpdateInvoiceCompanyProfileRequest request, HttpServletRequest httpRequest) {
        if (request.name() == null || request.name().isBlank()) {
            throw new BadRequestException("O nome da empresa é obrigatório");
        }
        if (request.nif() == null || request.nif().isBlank()) {
            throw new BadRequestException("O NIF da empresa é obrigatório");
        }
        InvoiceCompanyProfile updated = companyProfileService.update(
                request.name().trim(), request.legalName(), request.nif().trim(), request.address(), request.email());
        return InvoiceCompanyProfileDto.from(updated, baseUrl(httpRequest));
    }

    // Guardado fora da Media Library (não é um ficheiro "solto" que o admin gere em Media
    // Library — é o logótipo oficial usado em todas as faturas) mas ainda dentro de
    // app.media.upload-dir, porque tem de ser servido publicamente para aparecer no PDF gerado
    // e para o admin poder pré-visualizar. Substitui sempre o logótipo anterior (apaga o
    // ficheiro antigo do disco) — só existe um logótipo atual.
    @PostMapping(value = "/company-profile/logo", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('ADMIN')")
    public InvoiceCompanyProfileDto uploadLogo(@RequestParam("file") MultipartFile file, HttpServletRequest request) throws IOException {
        if (file.isEmpty()) {
            throw new BadRequestException("Ficheiro vazio");
        }
        String contentType = file.getContentType();
        if (contentType == null || !LOGO_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException("O logótipo tem de ser PNG ou JPEG");
        }

        Path dir = Paths.get(mediaUploadDir, LOGO_SUBFOLDER);
        Files.createDirectories(dir);

        String extension = contentType.toLowerCase().equals("image/png") ? ".png" : ".jpg";
        String stored = UUID.randomUUID() + extension;
        Path target = dir.resolve(stored).normalize();
        if (!target.startsWith(dir.normalize())) {
            throw new BadRequestException("Nome de ficheiro inválido");
        }
        Files.write(target, file.getBytes());

        String previousPath = companyProfileService.get().logoPath();
        companyProfileService.setLogoPath(LOGO_SUBFOLDER + "/" + stored);
        if (previousPath != null) {
            try {
                Files.deleteIfExists(Paths.get(mediaUploadDir, previousPath));
            } catch (IOException ignored) {
                // ficheiro antigo já não existe ou não pôde ser apagado — não bloqueia a troca do logótipo
            }
        }

        return InvoiceCompanyProfileDto.from(companyProfileService.get(), baseUrl(request));
    }

    // ---- Verificação pública de autenticidade (aberta via QR code / link no rodapé do PDF) --

    @GetMapping("/verify")
    public InvoiceVerificationDto verify(@RequestParam("doc") String documentNumber, @RequestParam("sig") String signature) {
        String[] parts = documentNumber.split("/");
        if (parts.length != 3) {
            return InvoiceVerificationDto.invalid();
        }
        Integer year;
        Integer number;
        try {
            year = Integer.valueOf(parts[1]);
            number = Integer.valueOf(parts[2]);
        } catch (NumberFormatException e) {
            return InvoiceVerificationDto.invalid();
        }
        return invoiceRepository.findBySeriesAndYearAndNumber(parts[0], year, number)
                .filter(invoice -> signingService.matches(invoice, signature))
                .map(InvoiceVerificationDto::valid)
                .orElseGet(InvoiceVerificationDto::invalid);
    }

    private String baseUrl(HttpServletRequest request) {
        return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
    }
}
