package cv.terrasystem.zebratravelb.excursion;

import java.time.LocalDate;

// Um participante = uma pessoa = uma Booking. Sem campo de "nº de pessoas" — para
// adicionar várias pessoas, chama-se este endpoint uma vez por pessoa (mesmo padrão
// de "múltiplos anexos" em ReservationGuestController: uma chamada por unidade).
public record AddGroupParticipantRequest(
        String guestName,
        String guestEmail,
        String guestPhone,
        LocalDate date,
        String status   // opcional: PENDING (default) ou CONFIRMED — participante que já pagou/chegou
) {
}
