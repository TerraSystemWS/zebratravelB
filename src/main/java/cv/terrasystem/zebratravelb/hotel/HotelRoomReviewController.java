package cv.terrasystem.zebratravelb.hotel;

import cv.terrasystem.zebratravelb.common.BadRequestException;
import cv.terrasystem.zebratravelb.common.NotFoundException;
import cv.terrasystem.zebratravelb.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms/{roomId}/reviews")
@RequiredArgsConstructor
public class HotelRoomReviewController {

    private static final List<String> ELIGIBLE_STATUSES = List.of(HotelReservation.CONFIRMED);

    private final HotelRoomReviewRepository reviewRepository;
    private final HotelRoomRepository roomRepository;
    private final HotelReservationRepository reservationRepository;

    @GetMapping
    public List<ReviewDto> getReviews(@PathVariable Integer roomId) {
        return reviewRepository.findByRoom_IdOrderByCreatedAtDesc(roomId).stream().map(ReviewDto::from).toList();
    }

    @GetMapping("/eligible")
    public boolean isEligible(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Integer roomId) {
        return reservationRepository.existsByRoom_IdAndUser_IdAndStatusIn(roomId, principal.getId(), ELIGIBLE_STATUSES);
    }

    @PostMapping
    public ReviewDto create(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Integer roomId, @RequestBody CreateReviewRequest request) {
        HotelRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("Quarto não encontrado: " + roomId));

        if (request.rating() == null || request.rating() < 1 || request.rating() > 5) {
            throw new BadRequestException("A avaliação deve ser entre 1 e 5");
        }
        if (!reservationRepository.existsByRoom_IdAndUser_IdAndStatusIn(roomId, principal.getId(), ELIGIBLE_STATUSES)) {
            throw new BadRequestException("Só podes avaliar quartos onde tenhas uma reserva confirmada ou paga");
        }
        if (reviewRepository.existsByRoom_IdAndUser_Id(roomId, principal.getId())) {
            throw new BadRequestException("Já avaliaste este quarto");
        }

        HotelRoomReview review = new HotelRoomReview();
        review.setRoom(room);
        review.setUser(principal.getUser());
        review.setRating(request.rating());
        review.setComment(request.comment());
        return ReviewDto.from(reviewRepository.save(review));
    }
}
