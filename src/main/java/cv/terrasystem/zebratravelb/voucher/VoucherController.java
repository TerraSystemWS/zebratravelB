package cv.terrasystem.zebratravelb.voucher;

import cv.terrasystem.zebratravelb.common.BadRequestException;
import cv.terrasystem.zebratravelb.common.ConflictException;
import cv.terrasystem.zebratravelb.common.NotFoundException;
import cv.terrasystem.zebratravelb.common.OwnershipGuard;
import cv.terrasystem.zebratravelb.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private static final Set<String> SCOPES = Set.of(Voucher.ALL, Voucher.EXCURSION, Voucher.ROOM, Voucher.PRODUCT);

    private final VoucherRepository voucherRepository;
    private final VoucherRedemptionRepository redemptionRepository;
    private final VoucherService voucherService;

    // Chamado pelos três checkouts antes de submeter, para mostrar "código válido, -20%"
    // sem ainda registar o uso (esse só acontece quando a reserva/encomenda é mesmo criada).
    @PostMapping("/validate")
    public Map<String, Object> validate(@AuthenticationPrincipal UserPrincipal principal, @RequestBody Map<String, Object> body) {
        String code = (String) body.get("code");
        String scope = (String) body.get("scope");
        Integer itemId = body.get("itemId") != null ? ((Number) body.get("itemId")).intValue() : null;
        Voucher voucher = voucherService.validateCode(code, scope, itemId, principal.getUser());
        return Map.of("valid", true, "discountPercent", voucher.getDiscountPercent());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public List<VoucherDto> getAll() {
        return voucherRepository.findAll().stream()
                .map(v -> VoucherDto.from(v, redemptionRepository.countByVoucher_IdAndReleasedFalse(v.getId())))
                .toList();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public VoucherDto create(@AuthenticationPrincipal UserPrincipal principal, @RequestBody VoucherRequest request) {
        Voucher voucher = new Voucher();
        applyTo(voucher, request, null);
        voucher.setCreatedBy(principal.getUser());
        return VoucherDto.from(voucherRepository.save(voucher), 0);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public VoucherDto update(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Integer id, @RequestBody VoucherRequest request) {
        Voucher voucher = find(id);
        OwnershipGuard.requireOwnerOrAdmin(principal, voucher.getCreatedBy());
        applyTo(voucher, request, id);
        long usesCount = redemptionRepository.countByVoucher_IdAndReleasedFalse(id);
        return VoucherDto.from(voucherRepository.save(voucher), usesCount);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Integer id) {
        Voucher voucher = find(id);
        OwnershipGuard.requireOwnerOrAdmin(principal, voucher.getCreatedBy());
        voucherRepository.delete(voucher);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/redemptions")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public List<VoucherRedemptionDto> getRedemptions(@PathVariable Integer id) {
        find(id);
        return redemptionRepository.findByVoucher_Id(id).stream().map(VoucherRedemptionDto::from).toList();
    }

    private void applyTo(Voucher voucher, VoucherRequest request, Integer selfId) {
        if (request.discountPercent() == null || request.discountPercent() < 0 || request.discountPercent() > 99) {
            throw new BadRequestException("Percentagem de desconto tem de estar entre 0 e 99");
        }
        if (request.scope() == null || !SCOPES.contains(request.scope())) {
            throw new BadRequestException("Âmbito inválido: " + request.scope());
        }
        boolean requiresCode = request.requiresCode() == null || request.requiresCode();

        if (!requiresCode) {
            // Promoção fixa: só faz sentido ligada a um produto específico da Loja — ver
            // dev-notes.md, decisão confirmada com o utilizador.
            if (!Voucher.PRODUCT.equals(request.scope()) || request.scopeItemId() == null) {
                throw new BadRequestException("Uma promoção sem código só pode ter âmbito 'Produto' com um produto específico escolhido");
            }
            voucher.setCode(null);
        } else {
            if (request.code() == null || request.code().isBlank()) {
                throw new BadRequestException("Código é obrigatório para um voucher");
            }
            String normalized = request.code().trim();
            voucherRepository.findByCodeIgnoreCase(normalized)
                    .filter(existing -> !existing.getId().equals(selfId))
                    .ifPresent(existing -> { throw new ConflictException("Já existe um voucher com este código"); });
            voucher.setCode(normalized);
        }
        if (request.validFrom() != null && request.validUntil() != null && request.validUntil().isBefore(request.validFrom())) {
            throw new BadRequestException("A data de fim não pode ser antes da data de início");
        }

        voucher.setRequiresCode(requiresCode);
        voucher.setDiscountPercent(request.discountPercent());
        voucher.setScope(request.scope());
        voucher.setScopeItemId(request.scopeItemId());
        voucher.setValidFrom(request.validFrom());
        voucher.setValidUntil(request.validUntil());
        voucher.setMaxUses(request.maxUses());
        voucher.setMaxUsesPerUser(request.maxUsesPerUser());
        voucher.setActive(request.active() == null || request.active());
    }

    private Voucher find(Integer id) {
        return voucherRepository.findById(id).orElseThrow(() -> new NotFoundException("Voucher não encontrado: " + id));
    }
}
