package cv.terrasystem.zebratravelb.favorite;

import cv.terrasystem.zebratravelb.common.BadRequestException;
import cv.terrasystem.zebratravelb.common.NotFoundException;
import cv.terrasystem.zebratravelb.excursion.ExcursionRepository;
import cv.terrasystem.zebratravelb.hotel.HotelRoomRepository;
import cv.terrasystem.zebratravelb.product.ProductRepository;
import cv.terrasystem.zebratravelb.security.UserPrincipal;
import cv.terrasystem.zebratravelb.tour.TourRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

// Favoritos genéricos — Quartos, Produtos, Excursões e Destinos (Tour), ver Favorite.java.
@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private static final Set<String> ITEM_TYPES = Set.of(Favorite.ROOM, Favorite.PRODUCT, Favorite.EXCURSION, Favorite.TOUR);

    private final FavoriteRepository favoriteRepository;
    private final HotelRoomRepository hotelRoomRepository;
    private final ProductRepository productRepository;
    private final ExcursionRepository excursionRepository;
    private final TourRepository tourRepository;

    @GetMapping
    public List<FavoriteDto> getMyFavorites(@AuthenticationPrincipal UserPrincipal principal) {
        return favoriteRepository.findByUserId(principal.getId()).stream()
                .map(this::resolve)
                .filter(dto -> dto != null)
                .toList();
    }

    @PostMapping
    public ResponseEntity<Void> add(@AuthenticationPrincipal UserPrincipal principal, @RequestBody AddFavoriteRequest request) {
        String itemType = requireValidType(request.itemType());
        if (request.itemId() == null) {
            throw new BadRequestException("itemId é obrigatório");
        }
        requireItemExists(itemType, request.itemId());

        // 204, não 200: um 200 com corpo vazio faz o cliente (zebratravel/lib/clientAuth.ts)
        // tentar sempre um res.json() e rebentar com "Unexpected end of JSON input" mesmo
        // quando o favorito já foi guardado com sucesso — ver ContactMessageController.
        if (favoriteRepository.findByUserIdAndItemTypeAndItemId(principal.getId(), itemType, request.itemId()).isPresent()) {
            return ResponseEntity.noContent().build();
        }
        Favorite favorite = new Favorite();
        favorite.setUser(principal.getUser());
        favorite.setItemType(itemType);
        favorite.setItemId(request.itemId());
        favoriteRepository.save(favorite);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{itemType}/{itemId}")
    public ResponseEntity<Void> remove(@AuthenticationPrincipal UserPrincipal principal, @PathVariable String itemType, @PathVariable Integer itemId) {
        // find + delete(entity) em vez de um deleteBy... derivado: um método de delete só
        // declarado na interface (sem corresponder a nenhum método base do SimpleJpaRepository)
        // não fica automaticamente dentro de uma transação, e o Hibernate rejeita o remove()
        // fora de uma transação ativa ("No EntityManager with actual transaction available").
        favoriteRepository.findByUserIdAndItemTypeAndItemId(principal.getId(), itemType.toUpperCase(), itemId)
                .ifPresent(favoriteRepository::delete);
        return ResponseEntity.noContent().build();
    }

    private FavoriteDto resolve(Favorite f) {
        return switch (f.getItemType()) {
            case Favorite.ROOM -> hotelRoomRepository.findById(f.getItemId()).map(FavoriteDto::fromRoom).orElse(null);
            case Favorite.PRODUCT -> productRepository.findById(f.getItemId()).map(FavoriteDto::fromProduct).orElse(null);
            case Favorite.EXCURSION -> excursionRepository.findById(f.getItemId()).map(FavoriteDto::fromExcursion).orElse(null);
            case Favorite.TOUR -> tourRepository.findById(f.getItemId()).map(FavoriteDto::fromTour).orElse(null);
            default -> null;
        };
    }

    private String requireValidType(String itemType) {
        String normalized = itemType != null ? itemType.toUpperCase() : null;
        if (normalized == null || !ITEM_TYPES.contains(normalized)) {
            throw new BadRequestException("itemType inválido: " + itemType);
        }
        return normalized;
    }

    private void requireItemExists(String itemType, Integer itemId) {
        boolean exists = switch (itemType) {
            case Favorite.ROOM -> hotelRoomRepository.existsById(itemId);
            case Favorite.PRODUCT -> productRepository.existsById(itemId);
            case Favorite.EXCURSION -> excursionRepository.existsById(itemId);
            case Favorite.TOUR -> tourRepository.existsById(itemId);
            default -> false;
        };
        if (!exists) {
            throw new NotFoundException(itemType + " não encontrado: " + itemId);
        }
    }
}
