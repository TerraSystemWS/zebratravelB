package cv.terrasystem.zebratravelb.invoice;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface InvoiceSequenceRepository extends JpaRepository<InvoiceSequence, Integer> {

    // Bloqueia a linha até a transação terminar, para dois pedidos concorrentes não lerem o
    // mesmo nextNumber e emitirem o mesmo número duas vezes — necessário mesmo sem otimizar
    // para alta concorrência (ver InvoiceSequence.java).
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM InvoiceSequence s WHERE s.series = :series AND s.year = :year")
    Optional<InvoiceSequence> findForUpdate(String series, Integer year);
}
