package cv.terrasystem.zebratravelb.invoice;

// logoPath é o caminho relativo dentro de app.media.upload-dir (ex: "invoice-logo/<uuid>.png"),
// não uma URL — a conversão para URL pública acontece só no DTO de resposta, o gerador de PDF
// resolve o ficheiro diretamente no disco a partir deste caminho.
public record InvoiceCompanyProfile(
        String name,
        String legalName,
        String nif,
        String address,
        String email,
        String logoPath
) {
}
