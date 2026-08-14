package cv.terrasystem.zebratravelb.contact;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, Integer> {

    long countByReadFalse();
}
