package org.udsm.udsm_hackathon2026.dto;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopDownloadsDto {
    private Long articleId;
    private String title;
    private String authors;
    private Long totalDownloads;
}
