package cv.terrasystem.zebratravelb.invoice;

import cv.terrasystem.zebratravelb.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Emitida sempre DEPOIS de o pagamento estar confirmado (nunca antes) — por isso, para já,
// só existe o tipo Fatura-Recibo (documento já nasce marcado como pago). A distinção entre
// "Consumidor Final" (Talão de Venda) e uma Fatura normal com NIF é só um dado (customerNif
// preenchido ou não), não um documentType diferente — simplificação deliberada da Fase 1,
// ver dev-notes.md.
@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
public class Invoice {

    public static final String FATURA_RECIBO = "FATURA_RECIBO";

    public static final String ORDER = "ORDER";
    public static final String EXCURSION_BOOKING = "EXCURSION_BOOKING";
    public static final String HOTEL_RESERVATION = "HOTEL_RESERVATION";

    public static final String ISSUED = "ISSUED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String series;

    @Column(nullable = false)
    private Integer number;

    @Column(nullable = false)
    private Integer year;

    @Column(name = "document_type", nullable = false)
    private String documentType = FATURA_RECIBO;

    @Column(name = "source_type", nullable = false)
    private String sourceType;

    @Column(name = "source_id", nullable = false)
    private Integer sourceId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "customer_email")
    private String customerEmail;

    @Column(name = "customer_nif")
    private String customerNif;

    @Column(nullable = false)
    private String currency = "CVE";

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "pdf_stored_filename")
    private String pdfStoredFilename;

    @Column(nullable = false)
    private String status = ISSUED;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<InvoiceLine> lines = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public String documentNumber() {
        return series + "/" + year + "/" + number;
    }
}
