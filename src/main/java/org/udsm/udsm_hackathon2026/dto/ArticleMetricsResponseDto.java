package org.udsm.udsm_hackathon2026.dto;
import lombok.*;
import java.util.List;


@Getter
@AllArgsConstructor
@Setter
@NoArgsConstructor
@Builder
public class ArticleMetricsResponseDto {
    // Article Basic Info
    private Long id;
    private String name;
    private String abstract_;
    private List<String> authors;  // List of author names

    // Key Metrics
    private Long totalDownloads;
    private Long totalCitations;
    private Long totalReaders;

    // Additional Info
    private String datePublished;
}

