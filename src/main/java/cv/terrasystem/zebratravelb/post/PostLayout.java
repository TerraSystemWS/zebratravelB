package cv.terrasystem.zebratravelb.post;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "post_layouts")
@Getter
@Setter
@NoArgsConstructor
public class PostLayout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true)
    private String layout;
}
