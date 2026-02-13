package org.udsm.udsm_hackathon2026.dto;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleMetricsDto {
    private Long submissionId;
    private String title;
    private Long totalReads;
}