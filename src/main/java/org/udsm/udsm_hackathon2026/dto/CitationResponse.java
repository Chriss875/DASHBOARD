package org.udsm.udsm_hackathon2026.dto;
import lombok.*;
import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CitationResponse {
    private Long publicationId;
    private String doi;
    private Integer citationCount;
    private Integer previousCount;
    private Integer citationIncrease;
    private LocalDateTime lastChecked;
    private String status;
    private String message;
}
