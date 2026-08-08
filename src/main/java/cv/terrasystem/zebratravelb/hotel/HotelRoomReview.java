package cv.terrasystem.zebratravelb.hotel;

import cv.terrasystem.zebratravelb.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "hotel_room_reviews")
@Getter
@Setter
@NoArgsConstructor
public class HotelRoomReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "room_id", nullable = false)
    private HotelRoom room;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_testimonial", nullable = false)
    private boolean testimonial = false;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
