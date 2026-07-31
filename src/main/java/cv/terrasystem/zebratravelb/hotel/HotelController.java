package cv.terrasystem.zebratravelb.hotel;

import cv.terrasystem.zebratravelb.common.BadRequestException;
import cv.terrasystem.zebratravelb.common.NotFoundException;
import cv.terrasystem.zebratravelb.common.OwnershipGuard;
import cv.terrasystem.zebratravelb.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class HotelController {

    private final HotelRepository hotelRepository;
    private final HotelRoomTypeRepository roomTypeRepository;
    private final HotelRoomRepository roomRepository;
    private final HotelReservationRepository reservationRepository;

    // ---- Hotels ---------------------------------------------------------

    @GetMapping("/api/hotels")
    public List<HotelDto> getHotels() {
        return hotelRepository.findAll().stream().map(HotelDto::from).toList();
    }

    @GetMapping("/api/hotels/{id}")
    public HotelDto getHotel(@PathVariable Integer id) {
        return HotelDto.from(findHotel(id));
    }

    @PostMapping("/api/hotels")
    @PreAuthorize("hasRole('ADMIN')")
    public HotelDto createHotel(@RequestBody HotelDto dto) {
        Hotel hotel = new Hotel();
        dto.applyTo(hotel);
        return HotelDto.from(hotelRepository.save(hotel));
    }

    @PutMapping("/api/hotels/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public HotelDto updateHotel(@PathVariable Integer id, @RequestBody HotelDto dto) {
        Hotel hotel = findHotel(id);
        dto.applyTo(hotel);
        return HotelDto.from(hotelRepository.save(hotel));
    }

    @DeleteMapping("/api/hotels/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteHotel(@PathVariable Integer id) {
        if (!hotelRepository.existsById(id)) throw new NotFoundException("Hotel não encontrado: " + id);
        hotelRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Room types -------------------------------------------------------

    @GetMapping("/api/hotels/{hotelId}/room-types")
    public List<RoomTypeDto> getRoomTypes(@PathVariable Integer hotelId) {
        return roomTypeRepository.findByHotel_Id(hotelId).stream().map(RoomTypeDto::from).toList();
    }

    @PostMapping("/api/hotels/{hotelId}/room-types")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public RoomTypeDto createRoomType(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Integer hotelId, @RequestBody RoomTypeDto dto) {
        HotelRoomType rt = new HotelRoomType();
        rt.setHotel(findHotel(hotelId));
        dto.applyTo(rt);
        rt.setCreatedBy(principal.getUser());
        return RoomTypeDto.from(roomTypeRepository.save(rt));
    }

    @GetMapping("/api/room-types/{id}")
    public RoomTypeDto getRoomType(@PathVariable Integer id) {
        return RoomTypeDto.from(roomTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tipo de quarto não encontrado: " + id)));
    }

    @PutMapping("/api/room-types/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public RoomTypeDto updateRoomType(@PathVariable Integer id, @RequestBody RoomTypeDto dto) {
        HotelRoomType rt = roomTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tipo de quarto não encontrado: " + id));
        dto.applyTo(rt);
        return RoomTypeDto.from(roomTypeRepository.save(rt));
    }

    @DeleteMapping("/api/room-types/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public ResponseEntity<Void> deleteRoomType(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Integer id) {
        HotelRoomType rt = roomTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tipo de quarto não encontrado: " + id));
        OwnershipGuard.requireOwnerOrAdmin(principal, rt.getCreatedBy());
        roomTypeRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Rooms --------------------------------------------------------------

    @GetMapping("/api/room-types/{roomTypeId}/rooms")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public List<RoomDto> getRooms(@PathVariable Integer roomTypeId) {
        return roomRepository.findByRoomType_Id(roomTypeId).stream().map(RoomDto::from).toList();
    }

    @PostMapping("/api/room-types/{roomTypeId}/rooms")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public RoomDto createRoom(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Integer roomTypeId, @RequestBody RoomDto dto) {
        HotelRoomType rt = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new NotFoundException("Tipo de quarto não encontrado: " + roomTypeId));
        if (roomRepository.existsByHotel_IdAndRoomNumberIgnoreCase(rt.getHotel().getId(), dto.roomNumber())) {
            throw new BadRequestException("Já existe um quarto com o código \"" + dto.roomNumber() + "\" neste hotel");
        }
        HotelRoom room = new HotelRoom();
        room.setRoomType(rt);
        room.setHotel(rt.getHotel());
        dto.applyTo(room);
        room.setCreatedBy(principal.getUser());
        return RoomDto.from(roomRepository.save(room));
    }

    @PutMapping("/api/rooms/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public RoomDto updateRoom(@PathVariable Integer id, @RequestBody RoomDto dto) {
        HotelRoom room = roomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Quarto não encontrado: " + id));
        if (roomRepository.existsByHotel_IdAndRoomNumberIgnoreCaseAndIdNot(room.getHotel().getId(), dto.roomNumber(), id)) {
            throw new BadRequestException("Já existe um quarto com o código \"" + dto.roomNumber() + "\" neste hotel");
        }
        dto.applyTo(room);
        return RoomDto.from(roomRepository.save(room));
    }

    @DeleteMapping("/api/rooms/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public ResponseEntity<Void> deleteRoom(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Integer id) {
        HotelRoom room = roomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Quarto não encontrado: " + id));
        OwnershipGuard.requireOwnerOrAdmin(principal, room.getCreatedBy());
        roomRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Public per-room browsing ------------------------------------------

    @GetMapping("/api/hotels/{hotelId}/rooms")
    public List<HotelRoomPublicDto> getHotelRooms(@PathVariable Integer hotelId) {
        return roomRepository.findByRoomType_Hotel_Id(hotelId).stream()
                .filter(room -> "ACTIVE".equals(room.getStatus()))
                .map(HotelRoomPublicDto::from)
                .toList();
    }

    @GetMapping("/api/rooms/{id}")
    public HotelRoomPublicDto getRoomPublic(@PathVariable Integer id) {
        return HotelRoomPublicDto.from(roomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Quarto não encontrado: " + id)));
    }

    // ---- Availability -----------------------------------------------------

    @GetMapping("/api/hotels/{hotelId}/availability")
    public List<AvailableRoomDto> getAvailability(
            @PathVariable Integer hotelId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut
    ) {
        if (!checkOut.isAfter(checkIn)) {
            throw new BadRequestException("A data de saída deve ser depois da data de entrada");
        }
        List<HotelRoom> rooms = roomRepository.findByRoomType_Hotel_Id(hotelId);
        return rooms.stream()
                .filter(room -> "ACTIVE".equals(room.getStatus()))
                .filter(room -> reservationRepository.findOverlapping(room.getId(), checkIn, checkOut).isEmpty())
                .map(AvailableRoomDto::from)
                .toList();
    }

    private Hotel findHotel(Integer id) {
        return hotelRepository.findById(id).orElseThrow(() -> new NotFoundException("Hotel não encontrado: " + id));
    }
}
