package cv.terrasystem.zebratravelb.invoice;

public record UpdateInvoiceCompanyProfileRequest(
        String name,
        String legalName,
        String nif,
        String address,
        String email
) {
}
