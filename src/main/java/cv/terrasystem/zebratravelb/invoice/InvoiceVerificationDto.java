package cv.terrasystem.zebratravelb.invoice;

import java.time.format.DateTimeFormatter;

public record InvoiceVerificationDto(
        boolean valid,
        String documentNumber,
        String sourceType,
        String customerName,
        String customerNif,
        String totalAmount,
        String currency,
        String issuedAt
) {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static InvoiceVerificationDto invalid() {
        return new InvoiceVerificationDto(false, null, null, null, null, null, null, null);
    }

    public static InvoiceVerificationDto valid(Invoice invoice) {
        return new InvoiceVerificationDto(
                true,
                invoice.documentNumber(),
                invoice.getSourceType(),
                invoice.getCustomerName(),
                invoice.getCustomerNif(),
                invoice.getTotalAmount().toPlainString(),
                invoice.getCurrency(),
                invoice.getCreatedAt().format(DATE_FORMAT));
    }
}
