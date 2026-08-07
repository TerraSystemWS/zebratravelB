package cv.terrasystem.zebratravelb.media;

import cv.terrasystem.zebratravelb.common.BadRequestException;
import cv.terrasystem.zebratravelb.common.ConflictException;
import cv.terrasystem.zebratravelb.common.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
public class MediaController {

    private final MediaFolderRepository folderRepository;
    private final MediaItemRepository itemRepository;
    private final WebpConverter webpConverter;
    private final MediaUsageChecker usageChecker;

    @Value("${app.media.upload-dir}")
    private String uploadDir;

    // ---- Folders -----------------------------------------------------

    @GetMapping("/folders")
    public List<MediaFolderDto> getFolders() {
        return folderRepository.findAll().stream().map(MediaFolderDto::from).toList();
    }

    @PostMapping("/folders")
    public MediaFolderDto createFolder(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        if (name == null || name.isBlank()) {
            throw new BadRequestException("Nome da pasta é obrigatório");
        }
        Integer parentId = body.get("parentId") != null ? Integer.valueOf(body.get("parentId").toString()) : null;

        MediaFolder folder = new MediaFolder();
        folder.setName(name.trim());
        if (parentId != null) {
            folder.setParent(folderRepository.findById(parentId)
                    .orElseThrow(() -> new NotFoundException("Pasta não encontrada: " + parentId)));
        }
        return MediaFolderDto.from(folderRepository.save(folder));
    }

    @DeleteMapping("/folders/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteFolder(@PathVariable Integer id) {
        MediaFolder folder = folderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Pasta não encontrada: " + id));
        // Move any items in this folder up to root instead of deleting the files.
        itemRepository.findByFolder_Id(id).forEach(item -> item.setFolder(null));
        folderRepository.delete(folder);
        return ResponseEntity.noContent().build();
    }

    // ---- Items ---------------------------------------------------------

    @GetMapping("/items")
    public List<MediaItemDto> getItems(@RequestParam(required = false) Integer folderId, HttpServletRequest request) {
        String base = baseUrl(request);
        List<MediaItem> items = folderId != null
                ? itemRepository.findByFolder_Id(folderId)
                : itemRepository.findByFolder_IdIsNull();
        return items.stream().map(item -> MediaItemDto.from(item, base)).toList();
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public MediaItemDto upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) Integer folderId,
            HttpServletRequest request
    ) throws IOException {
        if (file.isEmpty()) {
            throw new BadRequestException("Ficheiro vazio");
        }

        Path dir = Paths.get(uploadDir);
        Files.createDirectories(dir);

        String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "ficheiro";
        String extension = original.contains(".") ? original.substring(original.lastIndexOf('.')) : "";
        String contentType = file.getContentType();
        byte[] bytes = file.getBytes();

        // Images (other than SVG, already-webp, or unsupported formats) get converted to
        // WebP here — smaller files, same visual result. Falls back to storing the
        // original bytes untouched if conversion isn't applicable or fails for any reason.
        WebpConverter.Result converted = webpConverter.convert(bytes, contentType);
        if (converted != null) {
            bytes = converted.bytes();
            extension = converted.extension();
            contentType = converted.contentType();
        }

        String stored = UUID.randomUUID() + extension;

        Path target = dir.resolve(stored).normalize();
        if (!target.startsWith(dir.normalize())) {
            throw new BadRequestException("Nome de ficheiro inválido");
        }
        Files.write(target, bytes);

        MediaItem item = new MediaItem();
        item.setOriginalFilename(original);
        item.setStoredFilename(stored);
        item.setContentType(contentType);
        item.setSizeBytes((long) bytes.length);
        if (folderId != null) {
            item.setFolder(folderRepository.findById(folderId)
                    .orElseThrow(() -> new NotFoundException("Pasta não encontrada: " + folderId)));
        }

        return MediaItemDto.from(itemRepository.save(item), baseUrl(request));
    }

    @DeleteMapping("/items/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteItem(@PathVariable Integer id) {
        MediaItem item = itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ficheiro não encontrado: " + id));
        List<String> usages = usageChecker.findUsages(item.getStoredFilename());
        if (!usages.isEmpty()) {
            throw new ConflictException("Ficheiro em uso, não é possível apagar.", usages);
        }
        try {
            Files.deleteIfExists(Paths.get(uploadDir).resolve(item.getStoredFilename()));
        } catch (IOException ignored) {
            // if the file is already gone, still remove the DB record
        }
        itemRepository.delete(item);
        return ResponseEntity.noContent().build();
    }

    private String baseUrl(HttpServletRequest request) {
        return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
    }
}
