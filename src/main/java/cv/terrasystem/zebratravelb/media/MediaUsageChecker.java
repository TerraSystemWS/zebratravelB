package cv.terrasystem.zebratravelb.media;

import cv.terrasystem.zebratravelb.content.SiteContentRepository;
import cv.terrasystem.zebratravelb.excursion.ExcursionRepository;
import cv.terrasystem.zebratravelb.hotel.HotelRepository;
import cv.terrasystem.zebratravelb.hotel.HotelRoomRepository;
import cv.terrasystem.zebratravelb.hotel.HotelRoomTypeRepository;
import cv.terrasystem.zebratravelb.misc.GalleryItemRepository;
import cv.terrasystem.zebratravelb.misc.SponsorRepository;
import cv.terrasystem.zebratravelb.misc.TeamMemberRepository;
import cv.terrasystem.zebratravelb.misc.TestimonialRepository;
import cv.terrasystem.zebratravelb.misc.TravelPackageRepository;
import cv.terrasystem.zebratravelb.post.AuthorRepository;
import cv.terrasystem.zebratravelb.post.PostRepository;
import cv.terrasystem.zebratravelb.product.ProductRepository;
import cv.terrasystem.zebratravelb.tour.TourRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Checks whether a media file (identified by its stored filename, e.g. "abc123.webp") is
 * still referenced anywhere else in the app before allowing a real delete — matching by
 * filename rather than full URL, since the same file can be referenced with different
 * host/protocol prefixes depending on which environment it was picked in.
 */
@Component
@RequiredArgsConstructor
public class MediaUsageChecker {

    private final ExcursionRepository excursionRepository;
    private final TourRepository tourRepository;
    private final HotelRepository hotelRepository;
    private final HotelRoomTypeRepository hotelRoomTypeRepository;
    private final HotelRoomRepository hotelRoomRepository;
    private final ProductRepository productRepository;
    private final PostRepository postRepository;
    private final AuthorRepository authorRepository;
    private final GalleryItemRepository galleryItemRepository;
    private final SponsorRepository sponsorRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TestimonialRepository testimonialRepository;
    private final TravelPackageRepository travelPackageRepository;
    private final SiteContentRepository siteContentRepository;

    public List<String> findUsages(String storedFilename) {
        List<String> usages = new ArrayList<>();

        excursionRepository.findByImageContaining(storedFilename)
                .forEach(e -> usages.add("Excursão: " + e.getTitle()));
        tourRepository.findByImageContaining(storedFilename)
                .forEach(t -> usages.add("Tour: " + t.getTitle()));
        tourRepository.findByImagesArrayContaining(storedFilename)
                .forEach(t -> usages.add("Tour (galeria): " + t.getTitle()));
        hotelRepository.findByImageContaining(storedFilename)
                .forEach(h -> usages.add("Hotel: " + h.getName()));
        hotelRoomTypeRepository.findByImageContaining(storedFilename)
                .forEach(rt -> usages.add("Tipo de Quarto: " + rt.getName()));
        hotelRoomRepository.findByImagesArrayContaining(storedFilename)
                .forEach(r -> usages.add("Quarto (galeria): " + r.getRoomNumber()));
        productRepository.findByImageUrlContaining(storedFilename)
                .forEach(p -> usages.add("Produto: " + p.getTitle()));
        postRepository.findByImageContaining(storedFilename)
                .forEach(p -> usages.add("Post: " + p.getTitle()));
        authorRepository.findByProfileImageContaining(storedFilename)
                .forEach(a -> usages.add("Autor: " + a.getName()));
        galleryItemRepository.findByImgSrcContaining(storedFilename)
                .forEach(g -> usages.add("Galeria: item #" + g.getId()));
        sponsorRepository.findByImageContaining(storedFilename)
                .forEach(s -> usages.add("Patrocinador #" + s.getId()));
        teamMemberRepository.findByImageContaining(storedFilename)
                .forEach(tm -> usages.add("Equipa: " + tm.getName()));
        testimonialRepository.findByImageContainingOrBackgroundImageContaining(storedFilename, storedFilename)
                .forEach(t -> usages.add("Testemunho: " + t.getName()));
        travelPackageRepository.findByImageUrlContaining(storedFilename)
                .forEach(tp -> usages.add("Pacote de Viagem: " + tp.getTitle()));
        siteContentRepository.findByContentValueContaining(storedFilename)
                .forEach(sc -> usages.add("Conteúdo do site: " + sc.getContentKey()));

        return usages;
    }
}
