package cv.terrasystem.zebratravelb.invoice;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {
    boolean existsBySourceTypeAndSourceId(String sourceType, Integer sourceId);
    Optional<Invoice> findBySourceTypeAndSourceId(String sourceType, Integer sourceId);
    List<Invoice> findByUser_IdOrderByCreatedAtDesc(Integer userId);
    List<Invoice> findAllByOrderByCreatedAtDesc();
    Optional<Invoice> findBySeriesAndYearAndNumber(String series, Integer year, Integer number);
}
