package cv.terrasystem.zebratravelb.favorite;

import cv.terrasystem.zebratravelb.excursion.Excursion;
import cv.terrasystem.zebratravelb.hotel.HotelRoom;
import cv.terrasystem.zebratravelb.product.Product;
import cv.terrasystem.zebratravelb.tour.Tour;

import java.math.BigDecimal;

public record FavoriteDto(
        String itemType,
        Integer itemId,
        String title,
        String subtitle,
        String image,
        BigDecimal price,
        String link
) {
    public static FavoriteDto fromRoom(HotelRoom r) {
        String title = r.getRoomNumber() + " - " + r.getRoomType().getName();
        String image = (r.getImages() != null && r.getImages().length > 0) ? r.getImages()[0] : r.getRoomType().getImage();
        return new FavoriteDto(Favorite.ROOM, r.getId(), title, r.getHotel().getName(), image,
                r.getRoomType().getBasePrice(), "/hotel/" + r.getHotel().getId() + "/quarto/" + r.getId());
    }

    public static FavoriteDto fromProduct(Product p) {
        return new FavoriteDto(Favorite.PRODUCT, p.getId(), p.getTitle(), null, p.getImageUrl(), p.getPrice(), "/loja");
    }

    public static FavoriteDto fromExcursion(Excursion e) {
        return new FavoriteDto(Favorite.EXCURSION, e.getId(), e.getTitle(), e.getLocation(), e.getImage(), e.getPrice(),
                "/excurcoes/" + e.getSlug());
    }

    public static FavoriteDto fromTour(Tour t) {
        String image = (t.getImages() != null && t.getImages().length > 0) ? t.getImages()[0] : t.getImage();
        return new FavoriteDto(Favorite.TOUR, t.getId(), t.getTitle(), null, image, t.getPrice(), "/destinos/" + t.getId());
    }
}
