package org.udsm.udsm_hackathon2026.dto.author;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Complete dashboard combining all author metrics")
public class CompleteDashboardDTO {
    
    @Schema(description = "Dashboard summary statistics")
    private AuthorDashboardDTO dashboard;
    
    @Schema(description = "Review time metrics")
    private ReviewTimeDTO reviewTimes;
    
    @Schema(description = "Productivity analysis")
    private ProductivityDTO productivity;
    
    @Schema(description = "Author ranking information")
    private RankingDTO ranking;
    
    @Schema(description = "Timeline of submissions")
    private TimelineDTO timeline;
    
    @Schema(description = "Timestamp when this data was generated", example = "2024-02-14T18:30:00")
    private LocalDateTime lastUpdated;
}
