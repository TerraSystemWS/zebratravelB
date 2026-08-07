package cv.terrasystem.zebratravelb.post;

import cv.terrasystem.zebratravelb.common.NotFoundException;
import cv.terrasystem.zebratravelb.common.OwnershipGuard;
import cv.terrasystem.zebratravelb.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostRepository postRepository;
    private final AuthorRepository authorRepository;
    private final PostCategoryRepository postCategoryRepository;

    @GetMapping
    public List<PostDto> getAll(@RequestParam(defaultValue = "false") boolean includeArchived) {
        return postRepository.findAll().stream()
                .filter(p -> includeArchived || !"ARCHIVED".equals(p.getStatus()))
                .map(PostDto::from)
                .toList();
    }

    @GetMapping("/{slug}")
    public PostDto getBySlug(@PathVariable String slug) {
        return PostDto.from(find(slug));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public PostDto create(@AuthenticationPrincipal UserPrincipal principal, @RequestBody PostDto dto) {
        Post post = new Post();
        applyTo(post, dto);
        post.setCreatedBy(principal.getUser());
        return PostDto.from(postRepository.save(post));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public PostDto update(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Integer id, @RequestBody PostDto dto) {
        Post post = postRepository.findById(id).orElseThrow(() -> new NotFoundException("Post não encontrado: " + id));
        OwnershipGuard.requireOwnerOrAdmin(principal, post.getCreatedBy());
        applyTo(post, dto);
        return PostDto.from(postRepository.save(post));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Integer id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new NotFoundException("Post não encontrado: " + id));
        OwnershipGuard.requireOwnerOrAdmin(principal, post.getCreatedBy());
        postRepository.delete(post);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/archive")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public PostDto archive(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Integer id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new NotFoundException("Post não encontrado: " + id));
        OwnershipGuard.requireOwnerOrAdmin(principal, post.getCreatedBy());
        post.setStatus("ARCHIVED");
        return PostDto.from(postRepository.save(post));
    }

    @PostMapping("/{id}/restore")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public PostDto restore(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Integer id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new NotFoundException("Post não encontrado: " + id));
        OwnershipGuard.requireOwnerOrAdmin(principal, post.getCreatedBy());
        post.setStatus("ACTIVE");
        return PostDto.from(postRepository.save(post));
    }

    private Post find(String slug) {
        return postRepository.findBySlug(slug).orElseThrow(() -> new NotFoundException("Post não encontrado: " + slug));
    }

    private void applyTo(Post post, PostDto dto) {
        post.setTitle(dto.title());
        post.setImage(dto.image());
        post.setContent(dto.content());
        post.setDescription(dto.description());
        post.setDate(dto.date() != null ? dto.date() : java.time.LocalDate.now());
        if (dto.slug() != null) {
            post.setSlug(dto.slug());
        }
        if (dto.author() != null) {
            post.setAuthor(authorRepository.findAll().stream()
                    .filter(a -> dto.author().equals(a.getName()))
                    .findFirst()
                    .orElseGet(() -> {
                        Author a = new Author();
                        a.setName(dto.author());
                        return authorRepository.save(a);
                    }));
        }
        if (dto.category() != null) {
            post.setCategory(postCategoryRepository.findByName(dto.category())
                    .orElseGet(() -> {
                        PostCategory c = new PostCategory();
                        c.setName(dto.category());
                        return postCategoryRepository.save(c);
                    }));
        }
    }
}
