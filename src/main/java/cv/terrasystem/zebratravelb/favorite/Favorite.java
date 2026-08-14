package cv.terrasystem.zebratravelb.favorite;

import cv.terrasystem.zebratravelb.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

// Favorito genérico — cobre Quartos, Produtos, Excursões e Destinos (Tour) a partir da
// mesma tabela, discriminado por itemType+itemId em vez de uma FK dedicada por tipo
// (substituiu o antigo favorite_products, que só sabia favoritar Product).
@Entity
@Table(name = "favorites")
@Getter
@Setter
@NoArgsConstructor
public class Favorite {

    public static final String ROOM = "ROOM";
    public static final String PRODUCT = "PRODUCT";
    public static final String EXCURSION = "EXCURSION";
    public static final String TOUR = "TOUR";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "item_type", nullable = false)
    private String itemType;

    @Column(name = "item_id", nullable = false)
    private Integer itemId;

    @Column(name = "date_added", nullable = false)
    private LocalDate dateAdded;

    @PrePersist
    void onCreate() {
        dateAdded = LocalDate.now();
    }
}
