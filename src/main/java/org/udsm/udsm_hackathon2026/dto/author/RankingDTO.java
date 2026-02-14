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
@Schema(description = "Author ranking and performance metrics")
public class RankingDTO {
    
    @Schema(description = "Author's rank among all authors", example = "15")
    private Integer rank;
    
    @Schema(description = "Total number of authors in the system", example = "100")
    private Long totalAuthors;
    
    @Schema(description = "Percentile ranking (0-100)", example = "85.0")
    private Double percentile;
    
    @Schema(description = "Author's performance score (0-100)", example = "78.5")
    private Double authorScore;
    
    @Schema(description = "Average score across all authors", example = "65.2")
    private Double journalAverage;
    
    @Schema(description = "Badge level: Gold, Silver, Bronze, or None", example = "Silver")
    private String badge;
}
