package cv.terrasystem.zebratravelb.campaign;

import cv.terrasystem.zebratravelb.common.BadRequestException;
import cv.terrasystem.zebratravelb.common.NotFoundException;
import cv.terrasystem.zebratravelb.common.OwnershipGuard;
import cv.terrasystem.zebratravelb.excursion.Excursion;
import cv.terrasystem.zebratravelb.excursion.ExcursionRepository;
import cv.terrasystem.zebratravelb.hotel.HotelRoomType;
import cv.terrasystem.zebratravelb.hotel.HotelRoomTypeRepository;
import cv.terrasystem.zebratravelb.product.Product;
import cv.terrasystem.zebratravelb.product.ProductRepository;
import cv.terrasystem.zebratravelb.security.UserPrincipal;
import cv.terrasystem.zebratravelb.voucher.Voucher;
import cv.terrasystem.zebratravelb.voucher.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/campaigns")
@RequiredArgsConstructor
public class CampaignController {

    private static final Set<String> PLACEMENTS = Set.of(
            Campaign.HOME_HERO, Campaign.HOME_STRIP, Campaign.LOJA_TOP, Campaign.EXCURSOES_TOP, Campaign.HOTEL_TOP
    );

    private final CampaignRepository campaignRepository;
    private final VoucherRepository voucherRepository;
    private final ProductRepository productRepository;
    private final ExcursionRepository excursionRepository;
    private final HotelRoomTypeRepository roomTypeRepository;
    private final CampaignService campaignService;

    // Público — só as campanhas "ao vivo" desse placement, já resolvidas. Pode haver mais
    // que uma ativa ao mesmo tempo (decisão confirmada com o utilizador): o frontend mostra
    // todas, ordenadas por prioridade, num carrossel se for mais que uma.
    @GetMapping("/active")
    public List<CampaignPublicDto> getActive(@RequestParam String placement) {
        return campaignRepository.findByPlacement(placement).stream()
                .filter(campaignService::isLive)
                .sorted(Comparator.comparing(Campaign::getPriority).reversed())
                .map(c -> CampaignPublicDto.from(c, campaignService.resolveDisplay(c)))
                .toList();
    }

    // Público, sem corpo — só regista o clique (decisão: só cliques, sem impressões).
    @PostMapping("/{id}/click")
    public ResponseEntity<Void> click(@PathVariable Integer id) {
        Campaign campaign = find(id);
        campaign.setClickCount(campaign.getClickCount() + 1);
        campaignRepository.save(campaign);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public List<CampaignDto> getAll() {
        return campaignRepository.findAll().stream()
                .map(c -> CampaignDto.from(c, campaignService.resolveDisplay(c), campaignService.isLive(c)))
                .toList();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public CampaignDto create(@AuthenticationPrincipal UserPrincipal principal, @RequestBody CampaignRequest request) {
        Campaign campaign = new Campaign();
        applyTo(campaign, request);
        campaign.setCreatedBy(principal.getUser());
        Campaign saved = campaignRepository.save(campaign);
        return CampaignDto.from(saved, campaignService.resolveDisplay(saved), campaignService.isLive(saved));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public CampaignDto update(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Integer id, @RequestBody CampaignRequest request) {
        Campaign campaign = find(id);
        OwnershipGuard.requireOwnerOrAdmin(principal, campaign.getCreatedBy());
        applyTo(campaign, request);
        Campaign saved = campaignRepository.save(campaign);
        return CampaignDto.from(saved, campaignService.resolveDisplay(saved), campaignService.isLive(saved));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Integer id) {
        Campaign campaign = find(id);
        OwnershipGuard.requireOwnerOrAdmin(principal, campaign.getCreatedBy());
        campaignRepository.delete(campaign);
        return ResponseEntity.noContent().build();
    }

    private void applyTo(Campaign campaign, CampaignRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new BadRequestException("Nome é obrigatório");
        }
        if (request.imageUrl() == null || request.imageUrl().isBlank()) {
            throw new BadRequestException("Imagem é obrigatória");
        }
        if (request.placement() == null || !PLACEMENTS.contains(request.placement())) {
            throw new BadRequestException("Placement inválido: " + request.placement());
        }
        long targetCount = countNonNull(request.voucherId(), request.productId(), request.excursionId(), request.roomTypeId());
        if (targetCount > 1) {
            throw new BadRequestException("Uma campanha só pode estar ligada a um alvo (voucher, produto, excursão ou quarto), não a vários");
        }
        if (request.startDate() != null && request.endDate() != null && request.endDate().isBefore(request.startDate())) {
            throw new BadRequestException("A data de fim não pode ser antes da data de início");
        }

        campaign.setName(request.name());
        campaign.setImageUrl(request.imageUrl());
        campaign.setAltText(request.altText());
        campaign.setPlacement(request.placement());
        campaign.setVoucher(resolveVoucher(request.voucherId()));
        campaign.setProduct(resolveProduct(request.productId()));
        campaign.setExcursion(resolveExcursion(request.excursionId()));
        campaign.setRoomType(resolveRoomType(request.roomTypeId()));
        campaign.setTitle(blankToNull(request.title()));
        campaign.setSubtitle(blankToNull(request.subtitle()));
        campaign.setLinkUrl(blankToNull(request.linkUrl()));
        campaign.setStartDate(request.startDate());
        campaign.setEndDate(request.endDate());
        campaign.setPriority(request.priority() != null ? request.priority() : 0);
        campaign.setActive(request.active() == null || request.active());
    }

    private Voucher resolveVoucher(Integer id) {
        if (id == null) return null;
        return voucherRepository.findById(id).orElseThrow(() -> new NotFoundException("Voucher não encontrado: " + id));
    }

    private Product resolveProduct(Integer id) {
        if (id == null) return null;
        return productRepository.findById(id).orElseThrow(() -> new NotFoundException("Produto não encontrado: " + id));
    }

    private Excursion resolveExcursion(Integer id) {
        if (id == null) return null;
        return excursionRepository.findById(id).orElseThrow(() -> new NotFoundException("Excursão não encontrada: " + id));
    }

    private HotelRoomType resolveRoomType(Integer id) {
        if (id == null) return null;
        return roomTypeRepository.findById(id).orElseThrow(() -> new NotFoundException("Tipo de quarto não encontrado: " + id));
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    private Campaign find(Integer id) {
        return campaignRepository.findById(id).orElseThrow(() -> new NotFoundException("Campanha não encontrada: " + id));
    }

    private static long countNonNull(Object a, Object b, Object c, Object d) {
        long n = 0;
        if (a != null) n++;
        if (b != null) n++;
        if (c != null) n++;
        if (d != null) n++;
        return n;
    }
}
