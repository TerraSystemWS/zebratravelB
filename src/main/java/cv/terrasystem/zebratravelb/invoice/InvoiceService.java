package cv.terrasystem.zebratravelb.invoice;

import cv.terrasystem.zebratravelb.booking.Booking;
import cv.terrasystem.zebratravelb.hotel.HotelReservation;
import cv.terrasystem.zebratravelb.order.Order;
import cv.terrasystem.zebratravelb.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    // Cabo Verde tem uma junta cambial (currency board) com o Escudo indexado ao Euro a esta
    // taxa fixa desde 1998 — nunca flutua, por isso é seguro usar como constante em vez de
    // configuração. Os preços no site estão em EUR; os documentos fiscais têm de estar em CVE
    // (ver dev-notes.md — o modelo da e-Fatura mostra "Moeda: CVE" nos documentos reais).
    private static final BigDecimal EUR_TO_CVE = new BigDecimal("110.265");

    private final InvoiceRepository invoiceRepository;
    private final InvoiceSequenceRepository sequenceRepository;
    private final InvoicePdfGenerator pdfGenerator;

    @Value("${app.documents.upload-dir}")
    private String documentsDir;

    public record LineInput(String description, Integer quantity, BigDecimal unitPriceEur) {
    }

    // Os 3 convenience methods abaixo resolvem "quem é o cliente" e "quais são as linhas" a
    // partir da própria entidade — chamados tanto do pagamento online (PaymentController) como
    // da confirmação manual de transferência/dinheiro (BookingController/HotelReservationController),
    // para não duplicar esta lógica nos dois sítios. issue() já é idempotente por origem.
    //
    // @Transactional tem de estar aqui, não só em issue()/issueNew(): chamar issue(...) a partir
    // destes métodos é auto-invocação (this.issue(...) dentro da mesma classe), que o proxy AOP
    // do Spring não intercepta — sem isto, allocateNumber() falha com "no transaction in progress"
    // porque o SELECT ... FOR UPDATE nunca chega a correr dentro de uma transação real.

    @Transactional
    public Invoice issueForHotelReservation(HotelReservation reservation) {
        User user = reservation.getUser();
        String customerName = user != null
                ? (user.getFullName() != null ? user.getFullName() : user.getUsername())
                : reservation.getGuestName();
        String customerEmail = user != null ? user.getEmail() : reservation.getGuestEmail();
        long nights = Math.max(1, ChronoUnit.DAYS.between(reservation.getCheckIn(), reservation.getCheckOut()));
        String description = reservation.getHotel().getName() + " — " + reservation.getRoom().getRoomType().getName()
                + " (" + nights + (nights == 1 ? " noite" : " noites") + ")";
        return issue(Invoice.HOTEL_RESERVATION, reservation.getId(), user, customerName, customerEmail,
                reservation.getCustomerNif(), null, List.of(new LineInput(description, 1, reservation.getTotalAmount())));
    }

    @Transactional
    public Invoice issueForBooking(Booking booking) {
        User user = booking.getUser();
        String customerName = user != null
                ? (user.getFullName() != null ? user.getFullName() : user.getUsername())
                : booking.getGuestName();
        String customerEmail = user != null ? user.getEmail() : booking.getGuestEmail();
        return issue(Invoice.EXCURSION_BOOKING, booking.getId(), user, customerName, customerEmail,
                booking.getCustomerNif(), null, List.of(new LineInput(booking.getItemName(), 1, booking.getAmount())));
    }

    @Transactional
    public Invoice issueForOrder(Order order) {
        return issueForOrder(order, null);
    }

    // createdBy != null só na venda ao balcão (ADMIN/AGENTE) — auto-serviço no site público
    // nunca preenche isto.
    @Transactional
    public Invoice issueForOrder(Order order, User createdBy) {
        User user = order.getUser();
        String customerName = user != null
                ? (user.getFullName() != null ? user.getFullName() : user.getUsername())
                : order.getGuestName();
        String customerEmail = user != null ? user.getEmail() : order.getGuestEmail();
        List<LineInput> lines = order.getItems().stream()
                .map(item -> new LineInput(item.getName(), item.getQuantity(), item.getPrice()))
                .toList();
        return issue(Invoice.ORDER, order.getId(), user, customerName, customerEmail,
                order.getCustomerNif(), createdBy, lines);
    }

    private String seriesFor(String sourceType) {
        return switch (sourceType) {
            case Invoice.ORDER -> "L";
            case Invoice.EXCURSION_BOOKING -> "E";
            case Invoice.HOTEL_RESERVATION -> "H";
            default -> throw new IllegalArgumentException("Tipo de origem desconhecido: " + sourceType);
        };
    }

    // Idempotente: se já existir uma fatura para esta origem (ex: o callback do Vinti4 for
    // chamado duas vezes), devolve a já emitida em vez de duplicar.
    @Transactional
    public Invoice issue(String sourceType, Integer sourceId, User user, String customerName,
                          String customerEmail, String customerNif, User createdBy, List<LineInput> lines) {
        return invoiceRepository.findBySourceTypeAndSourceId(sourceType, sourceId)
                .orElseGet(() -> issueNew(sourceType, sourceId, user, customerName, customerEmail, customerNif, createdBy, lines));
    }

    private Invoice issueNew(String sourceType, Integer sourceId, User user, String customerName,
                              String customerEmail, String customerNif, User createdBy, List<LineInput> lines) {
        String series = seriesFor(sourceType);
        int year = LocalDate.now().getYear();
        int number = allocateNumber(series, year);

        Invoice invoice = new Invoice();
        invoice.setSeries(series);
        invoice.setNumber(number);
        invoice.setYear(year);
        invoice.setSourceType(sourceType);
        invoice.setSourceId(sourceId);
        invoice.setUser(user);
        invoice.setCustomerName(customerName);
        invoice.setCustomerEmail(customerEmail);
        invoice.setCustomerNif(customerNif != null && !customerNif.isBlank() ? customerNif : null);
        invoice.setCreatedBy(createdBy);

        BigDecimal total = BigDecimal.ZERO;
        for (LineInput input : lines) {
            BigDecimal unitPriceCve = toCve(input.unitPriceEur());
            BigDecimal lineTotalCve = unitPriceCve.multiply(BigDecimal.valueOf(input.quantity()));

            InvoiceLine line = new InvoiceLine();
            line.setInvoice(invoice);
            line.setDescription(input.description());
            line.setQuantity(input.quantity());
            line.setUnitPrice(unitPriceCve);
            line.setLineTotal(lineTotalCve);
            invoice.getLines().add(line);

            total = total.add(lineTotalCve);
        }
        invoice.setSubtotal(total);
        invoice.setTotalAmount(total);

        Invoice saved = invoiceRepository.save(invoice);
        attachPdf(saved);
        return saved;
    }

    // Bloqueia a linha do contador até ao fim da transação — sem isto, dois pedidos a emitir
    // ao mesmo tempo podiam ler o mesmo próximo número e gravar duas faturas com o mesmo
    // número (inaceitável num documento fiscal). Não otimizado para alta concorrência,
    // decisão explícita do utilizador.
    private int allocateNumber(String series, int year) {
        InvoiceSequence sequence = sequenceRepository.findForUpdate(series, year)
                .orElseGet(() -> {
                    InvoiceSequence s = new InvoiceSequence();
                    s.setSeries(series);
                    s.setYear(year);
                    return sequenceRepository.save(s);
                });
        int number = sequence.getNextNumber();
        sequence.setNextNumber(number + 1);
        sequenceRepository.save(sequence);
        return number;
    }

    private BigDecimal toCve(BigDecimal amountEur) {
        return amountEur.multiply(EUR_TO_CVE).setScale(2, RoundingMode.HALF_UP);
    }

    private void attachPdf(Invoice invoice) {
        try {
            byte[] pdfBytes = pdfGenerator.generate(invoice);
            Path dir = Paths.get(documentsDir, "invoices");
            Files.createDirectories(dir);
            String stored = UUID.randomUUID() + ".pdf";
            Path target = dir.resolve(stored).normalize();
            if (!target.startsWith(dir.normalize())) {
                throw new IOException("Nome de ficheiro inválido");
            }
            Files.write(target, pdfBytes);
            invoice.setPdfStoredFilename(stored);
            invoiceRepository.save(invoice);
        } catch (IOException e) {
            // A fatura já está gravada e numerada (o que importa para efeitos fiscais) mesmo
            // que o PDF falhe — fica sem pdfStoredFilename, pode ser gerado de novo mais tarde
            // se se adicionar um botão de "reprocessar PDF"; não vale a pena reverter a
            // numeração por causa disto.
            throw new UncheckedIOException("Erro ao gravar o PDF da fatura " + invoice.documentNumber(), e);
        }
    }
}
