package cv.terrasystem.zebratravelb.voucher;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VoucherRepository extends JpaRepository<Voucher, Integer> {

    Optional<Voucher> findByCodeIgnoreCase(String code);

    // Promoção fixa ativa para um produto específico (requiresCode=false, scope=PRODUCT,
    // scopeItemId=productId) — usada para mostrar o preço promocional e para a regra de
    // "não acumula com voucher".
    List<Voucher> findByRequiresCodeFalseAndScopeAndScopeItemIdAndActiveTrue(String scope, Integer scopeItemId);
}
