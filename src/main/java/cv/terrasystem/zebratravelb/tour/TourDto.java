package cv.terrasystem.zebratravelb.tour;

import java.util.List;
import java.util.stream.Collectors;

public record TourDto(
        Integer id,
        String title,
        String image,
        List<String> category,
        Integer tours,
        String description,
        Integer createdById
) {
    public static TourDto from(Tour tour) {
        return new TourDto(
                tour.getId(),
                tour.getTitle(),
                tour.getImage(),
                tour.getCategories().stream().map(Category::getName).collect(Collectors.toList()),
                tour.getToursCount(),
                tour.getDescription(),
                tour.getCreatedBy() != null ? tour.getCreatedBy().getId() : null
        );
    }

    public void applyTo(Tour tour) {
        tour.setTitle(title);
        tour.setImage(image);
        tour.setToursCount(tours != null ? tours : 0);
        tour.setDescription(description);
    }
}
