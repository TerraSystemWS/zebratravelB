package cv.terrasystem.zebratravelb.misc;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestimonialRepository extends JpaRepository<Testimonial, Integer> {
    List<Testimonial> findByImageContainingOrBackgroundImageContaining(String image, String backgroundImage);
}
