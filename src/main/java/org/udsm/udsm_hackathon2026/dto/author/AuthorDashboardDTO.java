package org.udsm.udsm_hackathon2026.dto.author;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Author dashboard summary with submission statistics")
public class AuthorDashboardDTO {
    
    @Schema(description = "Author's email address", example = "author@example.com")
    private String authorEmail;
    
    @Schema(description = "Total number of submissions", example = "25")
    private Long totalSubmissions;
    
    @Schema(description = "Number of published articles", example = "15")
    private Long publishedArticles;
    
    @Schema(description = "Number of articles under review", example = "5")
    private Long underReview;
    
    @Schema(description = "Number of declined submissions", example = "5")
    private Long declined;
    
    @Schema(description = "Acceptance rate percentage", example = "60.0")
    private Double acceptanceRate;
}
