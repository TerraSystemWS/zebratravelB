package cv.terrasystem.zebratravelb.misc;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "testimonials")
@Getter
@Setter
@NoArgsConstructor
public class Testimonial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Nullable porque testemunhos gerados automaticamente a partir de uma review não têm foto
    // de perfil nem cargo — só testemunhos criados manualmente pelo admin costumam ter isto.
    private String image;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(nullable = false)
    private String name;

    private String designation;

    @Column(nullable = false, precision = 2, scale = 1)
    private BigDecimal rating;

    @Column(name = "background_image")
    private String backgroundImage;

    private String link;

    // Preenchidos só quando o testemunho nasce de uma review real (ExcursionReview/HotelRoomReview) —
    // servem para saber a origem e para impedir criar dois testemunhos a partir da mesma review.
    @Column(name = "source_review_type")
    private String sourceReviewType;

    @Column(name = "source_review_id")
    private Integer sourceReviewId;
}
