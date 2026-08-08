package cv.terrasystem.zebratravelb.subscriber;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriberRepository extends JpaRepository<Subscriber, Integer> {
    boolean existsByEmail(String email);
}
