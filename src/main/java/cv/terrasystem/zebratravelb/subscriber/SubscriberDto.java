package cv.terrasystem.zebratravelb.subscriber;

import java.time.LocalDateTime;

public record SubscriberDto(Integer id, String email, LocalDateTime subscribedAt, String status) {
    public static SubscriberDto from(Subscriber subscriber) {
        return new SubscriberDto(subscriber.getId(), subscriber.getEmail(), subscriber.getSubscribedAt(), subscriber.getStatus());
    }
}
