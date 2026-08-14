package cv.terrasystem.zebratravelb.notification;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findAllByOrderByCreatedAtDesc(Pageable pageable);
    long countByReadFalse();
    List<Notification> findByReadFalse();
    List<Notification> findByCreatedAtBefore(LocalDateTime cutoff);
}
