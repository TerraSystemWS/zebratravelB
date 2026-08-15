package cv.terrasystem.zebratravelb.invoice;

public record InvoiceCompanyProfileDto(
        String name,
        String legalName,
        String nif,
        String address,
        String email,
        String logoUrl
) {
    public static InvoiceCompanyProfileDto from(InvoiceCompanyProfile profile, String baseUrl) {
        String logoUrl = profile.logoPath() != null ? baseUrl + "/uploads/" + profile.logoPath() : null;
        return new InvoiceCompanyProfileDto(
                profile.name(), profile.legalName(), profile.nif(), profile.address(), profile.email(), logoUrl);
    }
}
