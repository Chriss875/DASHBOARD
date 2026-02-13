package org.udsm.udsm_hackathon2026.dto;
import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ViewEventDto {
    private Long articleId;
    private String country;
}
