package org.udsm.udsm_hackathon2026.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.List;


@Getter
@AllArgsConstructor
@Setter
@NoArgsConstructor
@Builder
@Schema(description = "Complete article metrics response data transfer object")
public class ArticleMetricsResponseDto {
    // Article Basic Info
    @Schema(description = "Unique article identifier", example = "12345")
    private Long id;
    
    @Schema(description = "Article title/name", example = "Machine Learning in Healthcare Systems")
    private String name;
    
    @Schema(description = "Article abstract/summary", example = "This paper explores the applications of machine learning...")
    private String abstract_;
    
    @Schema(description = "List of author names", example = "[\"Dr. John Smith\", \"Prof. Jane Doe\", \"Dr. Michael Johnson\"]")
    private List<String> authors;  // List of author names

    // Key Metrics
    @Schema(description = "Total number of downloads", example = "1543")
    private Long totalDownloads;
    
    @Schema(description = "Total number of citations", example = "87")
    private Long totalCitations;
    
    @Schema(description = "Total number of readers", example = "2847")
    private Long totalReaders;

    // Additional Info
    @Schema(description = "Date when article was published", example = "2023-05-15")
    private String datePublished;
}

