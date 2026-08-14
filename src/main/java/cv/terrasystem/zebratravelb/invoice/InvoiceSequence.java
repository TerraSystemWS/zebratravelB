package cv.terrasystem.zebratravelb.invoice;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Contador por (série, ano) — a série reinicia sozinha em 1 assim que o primeiro documento
// do ano é emitido, porque a linha só é criada nesse momento (nunca há linhas "pré-criadas"
// para anos futuros). Concorrência (várias faturas emitidas ao mesmo tempo) não é tratada
// nesta fase — decisão explícita do utilizador, ver dev-notes.md.
@Entity
@Table(name = "invoice_sequences")
@Getter
@Setter
@NoArgsConstructor
public class InvoiceSequence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String series;

    @Column(nullable = false)
    private Integer year;

    @Column(name = "next_number", nullable = false)
    private Integer nextNumber = 1;
}
