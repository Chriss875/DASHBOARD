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
@Schema(description = "Review time statistics for author's submissions")
public class ReviewTimeDTO {
    
    @Schema(description = "Average number of days for review", example = "45.5")
    private Double averageReviewDays;
    
    @Schema(description = "Fastest review time in days", example = "15")
    private Long fastestReviewDays;
    
    @Schema(description = "Longest review time in days", example = "90")
    private Long longestReviewDays;
    
    @Schema(description = "List of submissions currently in review")
    private List<CurrentReviewDTO> currentInReview;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Details of a submission currently in review")
    public static class CurrentReviewDTO {
        
        @Schema(description = "Submission ID", example = "12345")
        private Long submissionId;
        
        @Schema(description = "Number of days pending", example = "30")
        private Long daysPending;
        
        @Schema(description = "Current status", example = "Under Review")
        private String status;
    }
}
