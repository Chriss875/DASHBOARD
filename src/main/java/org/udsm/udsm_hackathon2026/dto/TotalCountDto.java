package org.udsm.udsm_hackathon2026.dto;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TotalCountDto {
    private String label;
    private Long count;
}
