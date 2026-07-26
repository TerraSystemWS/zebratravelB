package cv.terrasystem.zebratravelb.content;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "site_content")
@Getter
@Setter
@NoArgsConstructor
public class SiteContent {

    @Id
    @Column(name = "content_key")
    private String contentKey;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "content_value", nullable = false, columnDefinition = "jsonb")
    private JsonNode contentValue;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    void onSave() {
        updatedAt = LocalDateTime.now();
    }
}
