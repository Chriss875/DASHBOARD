package org.udsm.udsm_hackathon2026.dto.author;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Timeline of author's submissions")
public class TimelineDTO {
    
    @Schema(description = "List of submission timelines")
    private List<SubmissionTimelineDTO> submissions;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Timeline details for a single submission")
    public static class SubmissionTimelineDTO {
        
        @Schema(description = "Submission ID", example = "12345")
        private Long submissionId;
        
        @Schema(description = "Current status", example = "Published")
        private String status;
        
        @Schema(description = "Current stage name", example = "Production")
        private String currentStage;
        
        @Schema(description = "Days in current stage", example = "15")
        private Long daysInCurrentStage;
        
        @Schema(description = "Date submitted", example = "2024-01-15")
        private LocalDateTime submittedDate;
        
        @Schema(description = "Expected completion date", example = "2024-03-15")
        private LocalDate expectedCompletionDate;
        
        @Schema(description = "Progress percentage (0-100)", example = "75")
        private Integer progress;
    }
}
