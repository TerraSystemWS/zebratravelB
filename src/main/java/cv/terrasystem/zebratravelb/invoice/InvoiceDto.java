package cv.terrasystem.zebratravelb.invoice;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record InvoiceDto(
        Integer id,
        String documentNumber,
        String documentType,
        String sourceType,
        Integer sourceId,
        String customerName,
        String customerEmail,
        String customerNif,
        String currency,
        BigDecimal subtotal,
        BigDecimal totalAmount,
        String status,
        LocalDateTime createdAt,
        List<InvoiceLineDto> lines
) {
    public static InvoiceDto from(Invoice invoice) {
        return new InvoiceDto(
                invoice.getId(),
                invoice.documentNumber(),
                invoice.getDocumentType(),
                invoice.getSourceType(),
                invoice.getSourceId(),
                invoice.getCustomerName(),
                invoice.getCustomerEmail(),
                invoice.getCustomerNif(),
                invoice.getCurrency(),
                invoice.getSubtotal(),
                invoice.getTotalAmount(),
                invoice.getStatus(),
                invoice.getCreatedAt(),
                invoice.getLines().stream().map(InvoiceLineDto::from).toList()
        );
    }

    public record InvoiceLineDto(String description, Integer quantity, BigDecimal unitPrice, BigDecimal lineTotal) {
        public static InvoiceLineDto from(InvoiceLine line) {
            return new InvoiceLineDto(line.getDescription(), line.getQuantity(), line.getUnitPrice(), line.getLineTotal());
        }
    }
}
