package cv.terrasystem.zebratravelb.invoice;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Component
public class InvoicePdfGenerator {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
    private static final Font HEADING_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
    private static final Font NORMAL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10);
    private static final Font SMALL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.ITALIC, Color.GRAY);
    private static final Font TABLE_HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);

    @Value("${app.invoice.company-name}")
    private String companyName;
    @Value("${app.invoice.company-address}")
    private String companyAddress;
    @Value("${app.invoice.company-nif}")
    private String companyNif;
    @Value("${app.invoice.company-email}")
    private String companyEmail;

    public byte[] generate(Invoice invoice) throws IOException {
        Document document = new Document(PageSize.A4, 40, 40, 50, 50);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, out);
            document.open();

            document.add(header(invoice));
            document.add(Chunk.NEWLINE);
            document.add(customerBlock(invoice));
            document.add(Chunk.NEWLINE);
            document.add(linesTable(invoice));
            document.add(Chunk.NEWLINE);
            document.add(totalsTable(invoice));
            document.add(Chunk.NEWLINE);
            document.add(footer());
        } catch (DocumentException e) {
            throw new IOException("Erro ao gerar o PDF da fatura", e);
        } finally {
            document.close();
        }
        return out.toByteArray();
    }

    private PdfPTable header(Invoice invoice) {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        try {
            table.setWidths(new float[]{3, 2});
        } catch (DocumentException ignored) {
            // larguras inválidas nunca acontecem aqui (array fixo), mas a API declara a exceção
        }

        Paragraph company = new Paragraph();
        company.add(new Paragraph(companyName, TITLE_FONT));
        company.add(new Paragraph(companyAddress, NORMAL_FONT));
        company.add(new Paragraph("NIF: " + companyNif, NORMAL_FONT));
        company.add(new Paragraph(companyEmail, NORMAL_FONT));
        PdfPCell companyCell = new PdfPCell(company);
        companyCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(companyCell);

        Paragraph doc = new Paragraph();
        doc.setAlignment(Element.ALIGN_RIGHT);
        doc.add(new Paragraph("FATURA-RECIBO", HEADING_FONT));
        doc.add(new Paragraph(invoice.documentNumber(), NORMAL_FONT));
        doc.add(new Paragraph("Data: " + invoice.getCreatedAt().format(DATE_FORMAT), NORMAL_FONT));
        PdfPCell docCell = new PdfPCell(doc);
        docCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(docCell);

        return table;
    }

    private Paragraph customerBlock(Invoice invoice) {
        Paragraph p = new Paragraph();
        p.add(new Paragraph("Cliente: " + invoice.getCustomerName(), NORMAL_FONT));
        if (invoice.getCustomerNif() != null && !invoice.getCustomerNif().isBlank()) {
            p.add(new Paragraph("NIF: " + invoice.getCustomerNif(), NORMAL_FONT));
        } else {
            p.add(new Paragraph("Consumidor Final", SMALL_FONT));
        }
        if (invoice.getCustomerEmail() != null && !invoice.getCustomerEmail().isBlank()) {
            p.add(new Paragraph(invoice.getCustomerEmail(), NORMAL_FONT));
        }
        return p;
    }

    private PdfPTable linesTable(Invoice invoice) throws DocumentException {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{4, 1, 1.5f, 1.5f});

        for (String label : new String[]{"Descrição", "Qtd.", "Preço Unit.", "Total"}) {
            PdfPCell cell = new PdfPCell(new Phrase(label, TABLE_HEADER_FONT));
            cell.setBackgroundColor(new Color(40, 40, 40));
            cell.setPadding(6);
            table.addCell(cell);
        }

        for (InvoiceLine line : invoice.getLines()) {
            table.addCell(cell(line.getDescription(), Element.ALIGN_LEFT));
            table.addCell(cell(String.valueOf(line.getQuantity()), Element.ALIGN_CENTER));
            table.addCell(cell(formatMoney(line.getUnitPrice(), invoice.getCurrency()), Element.ALIGN_RIGHT));
            table.addCell(cell(formatMoney(line.getLineTotal(), invoice.getCurrency()), Element.ALIGN_RIGHT));
        }

        return table;
    }

    private PdfPCell cell(String text, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, NORMAL_FONT));
        cell.setPadding(6);
        cell.setHorizontalAlignment(alignment);
        return cell;
    }

    private PdfPTable totalsTable(Invoice invoice) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(45);
        table.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.setWidths(new float[]{1, 1});

        table.addCell(labelCell("Subtotal"));
        table.addCell(valueCell(formatMoney(invoice.getSubtotal(), invoice.getCurrency())));
        table.addCell(labelCell("Total a Pagar"));
        PdfPCell total = valueCell(formatMoney(invoice.getTotalAmount(), invoice.getCurrency()));
        total.setPhrase(new Phrase(formatMoney(invoice.getTotalAmount(), invoice.getCurrency()), HEADING_FONT));
        table.addCell(total);

        return table;
    }

    private PdfPCell labelCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, NORMAL_FONT));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(4);
        return cell;
    }

    private PdfPCell valueCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, NORMAL_FONT));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(4);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        return cell;
    }

    // Nota: sem indicação de taxa de IVA — o enquadramento fiscal real da ZebraTravel ainda
    // não foi confirmado (ver tarefas.md, secção PraDepois). Não assumir nenhuma isenção
    // específica aqui sem confirmação — mostrar isso incorretamente num documento fiscal
    // seria pior do que não mostrar nada.
    private Paragraph footer() {
        Paragraph p = new Paragraph("Documento emitido pelo sistema ZebraTravel.", SMALL_FONT);
        p.setAlignment(Element.ALIGN_CENTER);
        return p;
    }

    private String formatMoney(java.math.BigDecimal amount, String currency) {
        return String.format("%,.2f %s", amount, currency);
    }
}
