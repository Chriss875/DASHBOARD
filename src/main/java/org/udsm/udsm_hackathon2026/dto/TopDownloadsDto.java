package org.udsm.udsm_hackathon2026.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Top article by downloads data transfer object")
public class TopDownloadsDto {
    @Schema(description = "Unique article identifier", example = "12345")
    private Long articleId;
    
    @Schema(description = "Article title", example = "AI in Medical Diagnosis")
    private String title;
    
    @Schema(description = "Article authors (comma-separated)", example = "Dr. Smith, Prof. Johnson")
    private String authors;
    
    @Schema(description = "Total number of downloads", example = "1543")
    private Long totalDownloads;
}
