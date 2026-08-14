package cv.terrasystem.zebratravelb.campaign;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CampaignRepository extends JpaRepository<Campaign, Integer> {
    List<Campaign> findByPlacement(String placement);
    List<Campaign> findByImageUrlContaining(String needle);
}
