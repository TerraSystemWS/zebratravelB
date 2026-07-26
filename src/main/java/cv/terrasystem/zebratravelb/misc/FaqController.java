package cv.terrasystem.zebratravelb.misc;

import cv.terrasystem.zebratravelb.common.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/faqs")
@RequiredArgsConstructor
public class FaqController {

    private final FaqRepository faqRepository;
    private final FaqTabRepository faqTabRepository;

    @GetMapping
    public List<FaqDto> getAll() {
        return faqRepository.findAll().stream().map(FaqDto::from).toList();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public FaqDto create(@RequestBody FaqDto dto) {
        Faq faq = new Faq();
        applyTo(faq, dto);
        return FaqDto.from(faqRepository.save(faq));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public FaqDto update(@PathVariable Integer id, @RequestBody FaqDto dto) {
        Faq faq = faqRepository.findById(id).orElseThrow(() -> new NotFoundException("FAQ não encontrada: " + id));
        applyTo(faq, dto);
        return FaqDto.from(faqRepository.save(faq));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (!faqRepository.existsById(id)) {
            throw new NotFoundException("FAQ não encontrada: " + id);
        }
        faqRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void applyTo(Faq faq, FaqDto dto) {
        faq.setQuestion(dto.question());
        faq.setAnswer(dto.answer());
        faq.setTab(faqTabRepository.findByLabel(dto.tab())
                .orElseGet(() -> {
                    FaqTab tab = new FaqTab();
                    tab.setLabel(dto.tab());
                    return faqTabRepository.save(tab);
                }));
    }
}
