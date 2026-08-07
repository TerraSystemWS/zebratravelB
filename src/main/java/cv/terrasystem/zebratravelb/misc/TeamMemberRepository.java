package cv.terrasystem.zebratravelb.misc;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Integer> {
    List<TeamMember> findByImageContaining(String needle);
}
