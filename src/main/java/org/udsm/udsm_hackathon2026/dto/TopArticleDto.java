package org.udsm.udsm_hackathon2026.dto;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopArticleDto {
    private Long articleId;
    private String title;
    private Long reads;
}