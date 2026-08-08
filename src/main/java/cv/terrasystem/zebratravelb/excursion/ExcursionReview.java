package cv.terrasystem.zebratravelb.excursion;

import cv.terrasystem.zebratravelb.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "excursion_reviews")
@Getter
@Setter
@NoArgsConstructor
public class ExcursionReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "excursion_id", nullable = false)
    private Excursion excursion;

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
