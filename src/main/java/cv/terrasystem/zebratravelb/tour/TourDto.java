package cv.terrasystem.zebratravelb.tour;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public record TourDto(
        Integer id,
        String title,
        String image,
        List<String> images,
        BigDecimal price,
        List<String> category,
        Integer tours,
        String description,
        Integer createdById,
        String status
) {
    public static TourDto from(Tour tour) {
        return new TourDto(
                tour.getId(),
                tour.getTitle(),
                tour.getImage(),
                tour.getImages() != null ? Arrays.asList(tour.getImages()) : List.of(),
                tour.getPrice(),
                tour.getCategories().stream().map(Category::getName).collect(Collectors.toList()),
                tour.getToursCount(),
                tour.getDescription(),
                tour.getCreatedBy() != null ? tour.getCreatedBy().getId() : null,
                tour.getStatus()
        );
    }

    public void applyTo(Tour tour) {
        tour.setTitle(title);
        tour.setImage(image);
        tour.setImages(images != null ? images.toArray(new String[0]) : new String[0]);
        tour.setPrice(price != null ? price : BigDecimal.ZERO);
        tour.setToursCount(tours != null ? tours : 0);
        tour.setDescription(description);
    }
}
