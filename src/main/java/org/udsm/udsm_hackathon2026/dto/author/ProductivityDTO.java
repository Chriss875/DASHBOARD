package org.udsm.udsm_hackathon2026.dto.author;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Author productivity metrics and publishing patterns")
public class ProductivityDTO {
    
    @Schema(description = "Number of articles published this year", example = "8")
    private Long articlesThisYear;
    
    @Schema(description = "Number of articles published this month", example = "2")
    private Long articlesThisMonth;
    
    @Schema(description = "Average articles published per year", example = "6.5")
    private Double averagePerYear;
    
    @Schema(description = "Breakdown of publications by year")
    private List<YearlyStats> yearlyBreakdown;
    
    @Schema(description = "Publishing trend: increasing, stable, or decreasing", example = "increasing")
    private String trend;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Publication count for a specific year")
    public static class YearlyStats {
        
        @Schema(description = "Year", example = "2024")
        private Integer year;
        
        @Schema(description = "Number of publications", example = "10")
        private Long count;
    }
}
