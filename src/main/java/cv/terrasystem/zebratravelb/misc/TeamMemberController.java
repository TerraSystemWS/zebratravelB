package cv.terrasystem.zebratravelb.misc;

import cv.terrasystem.zebratravelb.common.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/team-members")
@RequiredArgsConstructor
public class TeamMemberController {

    private final TeamMemberRepository repository;

    @GetMapping
    public List<TeamMember> getAll() {
        return repository.findAll();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public TeamMember create(@RequestBody TeamMember member) {
        member.setId(null);
        return repository.save(member);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public TeamMember update(@PathVariable Integer id, @RequestBody TeamMember member) {
        TeamMember existing = repository.findById(id).orElseThrow(() -> new NotFoundException("Membro não encontrado: " + id));
        existing.setName(member.getName());
        existing.setDesignation(member.getDesignation());
        existing.setImage(member.getImage());
        return repository.save(existing);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Membro não encontrado: " + id);
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
