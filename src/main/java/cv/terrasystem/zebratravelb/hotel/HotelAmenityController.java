package cv.terrasystem.zebratravelb.hotel;

import cv.terrasystem.zebratravelb.common.BadRequestException;
import cv.terrasystem.zebratravelb.common.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/hotel-amenities")
@RequiredArgsConstructor
public class HotelAmenityController {

    private final HotelAmenityRepository amenityRepository;

    @GetMapping
    public List<HotelAmenityDto> getAll() {
        return amenityRepository.findAll().stream()
                .sorted(Comparator.comparing(HotelAmenity::getLabel))
                .map(HotelAmenityDto::from)
                .toList();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public HotelAmenityDto create(@RequestBody HotelAmenityDto dto) {
        HotelAmenity amenity = new HotelAmenity();
        dto.applyTo(amenity);
        validate(amenity);
        return HotelAmenityDto.from(save(amenity));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public HotelAmenityDto update(@PathVariable Integer id, @RequestBody HotelAmenityDto dto) {
        HotelAmenity amenity = find(id);
        dto.applyTo(amenity);
        validate(amenity);
        return HotelAmenityDto.from(save(amenity));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (!amenityRepository.existsById(id)) throw new NotFoundException("Comodidade não encontrada: " + id);
        amenityRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void validate(HotelAmenity amenity) {
        if (amenity.getCode() == null || amenity.getCode().isBlank()) {
            throw new BadRequestException("Código é obrigatório");
        }
        if (amenity.getLabel() == null || amenity.getLabel().isBlank()) {
            throw new BadRequestException("Nome é obrigatório");
        }
        if (amenity.getIcon() == null || amenity.getIcon().isBlank()) {
            throw new BadRequestException("Ícone é obrigatório");
        }
    }

    private HotelAmenity save(HotelAmenity amenity) {
        try {
            return amenityRepository.save(amenity);
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException("Já existe uma comodidade com o código \"" + amenity.getCode() + "\"");
        }
    }

    private HotelAmenity find(Integer id) {
        return amenityRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Comodidade não encontrada: " + id));
    }
}
