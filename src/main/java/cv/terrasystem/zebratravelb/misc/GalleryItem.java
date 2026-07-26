package cv.terrasystem.zebratravelb.misc;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "gallery_items")
@Getter
@Setter
@NoArgsConstructor
public class GalleryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "img_src", nullable = false)
    private String imgSrc;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "gallery_item_categories",
            joinColumns = @JoinColumn(name = "gallery_item_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<GalleryCategory> categories = new HashSet<>();
}
