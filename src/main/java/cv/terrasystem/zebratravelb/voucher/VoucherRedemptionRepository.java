package cv.terrasystem.zebratravelb.voucher;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VoucherRedemptionRepository extends JpaRepository<VoucherRedemption, Integer> {

    long countByVoucher_IdAndReleasedFalse(Integer voucherId);
    long countByVoucher_IdAndUser_IdAndReleasedFalse(Integer voucherId, Integer userId);

    // JOIN FETCH no user: VoucherRedemptionDto precisa do nome (não só o id), e
    // spring.jpa.open-in-view=false fecha a sessão antes do controller mapear o DTO —
    // sem isto, aceder a user.getFullName() no proxy lazy rebenta com LazyInitializationException.
    @Query("SELECT r FROM VoucherRedemption r LEFT JOIN FETCH r.user WHERE r.voucher.id = :voucherId ORDER BY r.redeemedAt DESC")
    List<VoucherRedemption> findByVoucher_Id(@Param("voucherId") Integer voucherId);

    Optional<VoucherRedemption> findByBooking_IdAndReleasedFalse(Integer bookingId);
    Optional<VoucherRedemption> findByHotelReservation_IdAndReleasedFalse(Integer hotelReservationId);
    Optional<VoucherRedemption> findByOrder_IdAndReleasedFalse(Integer orderId);
}
