package cv.terrasystem.zebratravelb.content;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/content")
@RequiredArgsConstructor
public class ContentController {

    private static final String KEY = "site";

    private final SiteContentRepository siteContentRepository;
    private final ObjectMapper objectMapper;

    @GetMapping
    public JsonNode getContent() {
        return siteContentRepository.findById(KEY)
                .map(SiteContent::getContentValue)
                .orElseGet(objectMapper::createObjectNode);
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public JsonNode updateContent(@RequestBody ObjectNode body) {
        SiteContent content = siteContentRepository.findById(KEY).orElseGet(() -> {
            SiteContent c = new SiteContent();
            c.setContentKey(KEY);
            return c;
        });
        content.setContentValue(body);
        return siteContentRepository.save(content).getContentValue();
    }
}
