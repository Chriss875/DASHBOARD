package org.udsm.udsm_hackathon2026.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Top article by readership data transfer object")
public class TopArticleDto {
    @Schema(description = "Unique article identifier", example = "12345")
    private Long articleId;
    
    @Schema(description = "Article title", example = "Machine Learning in Healthcare")
    private String title;
    
    @Schema(description = "Article authors (comma-separated)", example = "John Doe, Jane Smith")
    private String authors;
    
    @Schema(description = "Article abstract/summary", example = "This study explores the application of machine learning...")
    private String articleAbstract;
    
    @Schema(description = "Total number of reads", example = "2847")
    private Long reads;
}