package cv.terrasystem.zebratravelb.voucher;

import cv.terrasystem.zebratravelb.booking.Booking;
import cv.terrasystem.zebratravelb.common.BadRequestException;
import cv.terrasystem.zebratravelb.hotel.HotelReservation;
import cv.terrasystem.zebratravelb.order.Order;
import cv.terrasystem.zebratravelb.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Optional;

// Regras de negócio partilhadas pelos três fluxos de checkout (Hotel, Excursão, Loja) —
// ver dev-notes.md para o desenho completo. Promoções fixas (requiresCode=false) só
// existem para Produtos e não são geridas aqui como "aplicação a um pedido" — só
// `findActivePromotion` é usado para elas (mostrar/():calcular preço promocional).
@Service
@RequiredArgsConstructor
public class VoucherService {

    private final VoucherRepository voucherRepository;
    private final VoucherRedemptionRepository redemptionRepository;

    /**
     * Valida um código de voucher para um scope/item — lança BadRequestException com uma
     * mensagem clara em qualquer caso de invalidade. Não regista o uso (ver recordRedemption).
     */
    public Voucher validateCode(String code, String scope, Integer itemId, User user) {
        if (code == null || code.isBlank()) {
            throw new BadRequestException("Código do voucher é obrigatório");
        }
        Voucher voucher = voucherRepository.findByCodeIgnoreCase(code.trim())
                .filter(Voucher::isRequiresCode)
                .orElseThrow(() -> new BadRequestException("Código de voucher inválido"));
        checkApplicable(voucher, scope, itemId, user);
        return voucher;
    }

    private void checkApplicable(Voucher voucher, String scope, Integer itemId, User user) {
        if (!voucher.isActive()) {
            throw new BadRequestException("Este voucher já não está ativo");
        }
        LocalDate today = LocalDate.now();
        if (voucher.getValidFrom() != null && today.isBefore(voucher.getValidFrom())) {
            throw new BadRequestException("Este voucher ainda não é válido");
        }
        if (voucher.getValidUntil() != null && today.isAfter(voucher.getValidUntil())) {
            throw new BadRequestException("Este voucher já expirou");
        }
        if (!Voucher.ALL.equals(voucher.getScope()) && !voucher.getScope().equals(scope)) {
            throw new BadRequestException("Este voucher não se aplica a este tipo de reserva");
        }
        // itemId == null representa "verificação genérica" (usado pela Loja antes de saber
        // a que linha do carrinho aplicar) — só se rejeita aqui quando o chamador já sabe o
        // item concreto e ele não bate certo. A decisão linha-a-linha da Loja usa appliesToItem().
        if (itemId != null && voucher.getScopeItemId() != null && !voucher.getScopeItemId().equals(itemId)) {
            throw new BadRequestException("Este voucher não se aplica a este item específico");
        }
        if (voucher.getMaxUses() != null
                && redemptionRepository.countByVoucher_IdAndReleasedFalse(voucher.getId()) >= voucher.getMaxUses()) {
            throw new BadRequestException("Este voucher já atingiu o limite de utilizações");
        }
        if (user != null && voucher.getMaxUsesPerUser() != null
                && redemptionRepository.countByVoucher_IdAndUser_IdAndReleasedFalse(voucher.getId(), user.getId()) >= voucher.getMaxUsesPerUser()) {
            throw new BadRequestException("Já usaste este voucher o máximo de vezes permitido");
        }
    }

    /**
     * Versão sem exceção de checkApplicable, só para o scope+item — usada pela Loja para
     * decidir, linha a linha do carrinho, se um voucher já validado a nível de encomenda se
     * aplica àquele produto específico (não repete a verificação de limites de uso, essa já
     * foi feita uma vez para a encomenda inteira em validateCode).
     */
    public boolean appliesToItem(Voucher voucher, String scope, Integer itemId) {
        if (!Voucher.ALL.equals(voucher.getScope()) && !voucher.getScope().equals(scope)) {
            return false;
        }
        return voucher.getScopeItemId() == null || voucher.getScopeItemId().equals(itemId);
    }

    public BigDecimal applyDiscount(Voucher voucher, BigDecimal amount) {
        BigDecimal factor = BigDecimal.valueOf(100 - voucher.getDiscountPercent()).divide(BigDecimal.valueOf(100));
        return amount.multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }

    /** Promoção fixa ativa para um produto (requiresCode=false, scope=PRODUCT) — se existir. */
    public Optional<Voucher> findActivePromotion(Integer productId) {
        LocalDate today = LocalDate.now();
        return voucherRepository.findByRequiresCodeFalseAndScopeAndScopeItemIdAndActiveTrue(Voucher.PRODUCT, productId)
                .stream()
                .filter(v -> v.isActive()
                        && (v.getValidFrom() == null || !today.isBefore(v.getValidFrom()))
                        && (v.getValidUntil() == null || !today.isAfter(v.getValidUntil())))
                .findFirst();
    }

    public VoucherRedemption recordRedemption(Voucher voucher, User user, BigDecimal discountAmount,
                                               Booking booking, HotelReservation hotelReservation, Order order) {
        VoucherRedemption redemption = new VoucherRedemption();
        redemption.setVoucher(voucher);
        redemption.setUser(user);
        redemption.setDiscountAmount(discountAmount);
        redemption.setBooking(booking);
        redemption.setHotelReservation(hotelReservation);
        redemption.setOrder(order);
        return redemptionRepository.save(redemption);
    }

    /** Chamar sempre que uma reserva/encomenda que possa ter usado um voucher é cancelada. */
    public void releaseForBooking(Integer bookingId) {
        redemptionRepository.findByBooking_IdAndReleasedFalse(bookingId)
                .ifPresent(r -> { r.setReleased(true); redemptionRepository.save(r); });
    }

    public void releaseForHotelReservation(Integer reservationId) {
        redemptionRepository.findByHotelReservation_IdAndReleasedFalse(reservationId)
                .ifPresent(r -> { r.setReleased(true); redemptionRepository.save(r); });
    }

    public void releaseForOrder(Integer orderId) {
        redemptionRepository.findByOrder_IdAndReleasedFalse(orderId)
                .ifPresent(r -> { r.setReleased(true); redemptionRepository.save(r); });
    }
}
