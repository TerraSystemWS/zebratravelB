package cv.terrasystem.zebratravelb.invoice;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class InvoicePdfGenerator {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
    private static final Font HEADING_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
    private static final Font NORMAL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10);
    private static final Font SMALL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.ITALIC, Color.GRAY);
    private static final Font TABLE_HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
    private static final Font CODE_FONT = FontFactory.getFont(FontFactory.COURIER, 8, Color.DARK_GRAY);

    private final InvoiceCompanyProfileService companyProfileService;

    @Value("${app.media.upload-dir}")
    private String mediaUploadDir;

    @Value("${app.frontend-url}")
    private String frontendUrl;

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
            document.add(Chunk.NEWLINE);
            document.add(verificationBlock(invoice));
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

        InvoiceCompanyProfile profile = companyProfileService.get();

        PdfPCell companyCell = new PdfPCell();
        companyCell.setBorder(Rectangle.NO_BORDER);
        Image logo = loadLogo(profile.logoPath());
        if (logo != null) {
            logo.scaleToFit(120, 50);
            companyCell.addElement(logo);
        }
        companyCell.addElement(new Paragraph(profile.name(), TITLE_FONT));
        if (profile.legalName() != null && !profile.legalName().isBlank() && !profile.legalName().equalsIgnoreCase(profile.name())) {
            companyCell.addElement(new Paragraph(profile.legalName(), NORMAL_FONT));
        }
        companyCell.addElement(new Paragraph(profile.address(), NORMAL_FONT));
        companyCell.addElement(new Paragraph("NIF: " + profile.nif(), NORMAL_FONT));
        companyCell.addElement(new Paragraph(profile.email(), NORMAL_FONT));
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

    // Nunca falha a geração do PDF por causa do logótipo — se o ficheiro não existir ou não
    // for legível como imagem, a fatura sai só com o texto (sempre foi assim antes de haver logo).
    private Image loadLogo(String logoPath) {
        if (logoPath == null || logoPath.isBlank()) {
            return null;
        }
        try {
            Path path = Paths.get(mediaUploadDir, logoPath);
            if (!Files.exists(path)) {
                return null;
            }
            return Image.getInstance(Files.readAllBytes(path));
        } catch (Exception e) {
            return null;
        }
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
    //
    // Código de verificação + QR code: aponta para uma página pública em zebratravel.net que
    // recalcula a assinatura HMAC (InvoiceSigningService) e confirma se o documento é genuíno —
    // qualquer alteração aos valores da fatura (ou uma tentativa de forjar uma do zero) faz a
    // verificação falhar, mesmo para quem tenha acesso ao painel de administração.
    private PdfPTable verificationBlock(Invoice invoice) {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        try {
            table.setWidths(new float[]{3, 1});
        } catch (DocumentException ignored) {
        }

        Paragraph note = new Paragraph();
        note.add(new Paragraph("Documento emitido pelo sistema ZebraTravel.", SMALL_FONT));
        note.add(new Paragraph("Verifique a autenticidade em " + frontendUrl + "/faturas/verificar", SMALL_FONT));
        PdfPCell noteCell = new PdfPCell(note);
        noteCell.setBorder(Rectangle.NO_BORDER);
        noteCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(noteCell);

        PdfPCell qrCell = new PdfPCell();
        qrCell.setBorder(Rectangle.NO_BORDER);
        qrCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        String signature = invoice.getSignature();
        if (signature != null && !signature.isBlank()) {
            try {
                Image qr = Image.getInstance(generateQrPng(verificationUrl(invoice), 120));
                qr.scaleToFit(65, 65);
                qr.setAlignment(Element.ALIGN_RIGHT);
                qrCell.addElement(qr);
            } catch (Exception ignored) {
                // sem QR se algo falhar — o código de texto abaixo continua a servir para verificar manualmente
            }
            Paragraph code = new Paragraph(formatVerificationCode(signature), CODE_FONT);
            code.setAlignment(Element.ALIGN_RIGHT);
            qrCell.addElement(code);
        }
        table.addCell(qrCell);

        return table;
    }

    private byte[] generateQrPng(String data, int size) throws Exception {
        BitMatrix matrix = new QRCodeWriter().encode(data, BarcodeFormat.QR_CODE, size, size);
        BufferedImage img = MatrixToImageWriter.toBufferedImage(matrix);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        return baos.toByteArray();
    }

    private String verificationUrl(Invoice invoice) {
        String doc = URLEncoder.encode(invoice.documentNumber(), StandardCharsets.UTF_8);
        return frontendUrl + "/faturas/verificar?doc=" + doc + "&sig=" + invoice.getSignature();
    }

    // Primeiros 16 caracteres da assinatura, em maiúsculas e agrupados de 4 em 4 — só para
    // leitura/digitação manual caso o QR code não seja prático (impressão em papel, por
    // exemplo); a verificação real usa a assinatura completa gravada na fatura.
    private String formatVerificationCode(String signature) {
        String code = signature.substring(0, Math.min(16, signature.length())).toUpperCase();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < code.length(); i++) {
            if (i > 0 && i % 4 == 0) {
                sb.append('-');
            }
            sb.append(code.charAt(i));
        }
        return sb.toString();
    }

    private String formatMoney(java.math.BigDecimal amount, String currency) {
        return String.format("%,.2f %s", amount, currency);
    }
}
