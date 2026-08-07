package cv.terrasystem.zebratravelb.misc;

import cv.terrasystem.zebratravelb.common.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/gallery")
@RequiredArgsConstructor
public class GalleryController {

    private final GalleryItemRepository galleryItemRepository;
    private final GalleryCategoryRepository galleryCategoryRepository;

    @GetMapping
    public List<GalleryItemDto> getAll(@RequestParam(defaultValue = "false") boolean includeArchived) {
        return galleryItemRepository.findAll().stream()
                .filter(g -> includeArchived || !"ARCHIVED".equals(g.getStatus()))
                .map(GalleryItemDto::from)
                .toList();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public GalleryItemDto create(@RequestBody GalleryItemDto dto) {
        GalleryItem item = new GalleryItem();
        item.setImgSrc(dto.imgSrc());
        item.setCategories(resolveCategories(dto.categories()));
        return GalleryItemDto.from(galleryItemRepository.save(item));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public GalleryItemDto update(@PathVariable Integer id, @RequestBody GalleryItemDto dto) {
        GalleryItem item = galleryItemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Imagem não encontrada: " + id));
        item.setImgSrc(dto.imgSrc());
        item.setCategories(resolveCategories(dto.categories()));
        return GalleryItemDto.from(galleryItemRepository.save(item));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (!galleryItemRepository.existsById(id)) {
            throw new NotFoundException("Imagem não encontrada: " + id);
        }
        galleryItemRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/archive")
    @PreAuthorize("hasRole('ADMIN')")
    public GalleryItemDto archive(@PathVariable Integer id) {
        GalleryItem item = galleryItemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Imagem não encontrada: " + id));
        item.setStatus("ARCHIVED");
        return GalleryItemDto.from(galleryItemRepository.save(item));
    }

    @PostMapping("/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public GalleryItemDto restore(@PathVariable Integer id) {
        GalleryItem item = galleryItemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Imagem não encontrada: " + id));
        item.setStatus("ACTIVE");
        return GalleryItemDto.from(galleryItemRepository.save(item));
    }

    private Set<GalleryCategory> resolveCategories(List<String> names) {
        if (names == null) return new HashSet<>();
        Set<GalleryCategory> categories = new HashSet<>();
        for (String name : names) {
            categories.add(galleryCategoryRepository.findByName(name).orElseGet(() -> {
                GalleryCategory c = new GalleryCategory();
                c.setName(name);
                return galleryCategoryRepository.save(c);
            }));
        }
        return categories;
    }
}
