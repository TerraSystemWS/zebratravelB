package cv.terrasystem.zebratravelb.subscriber;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubscriberService {

    private final SubscriberRepository subscriberRepository;

    public void subscribeIfAbsent(String email) {
        if (email == null || email.isBlank() || subscriberRepository.existsByEmail(email)) {
            return;
        }
        Subscriber subscriber = new Subscriber();
        subscriber.setEmail(email);
        subscriberRepository.save(subscriber);
    }
}
