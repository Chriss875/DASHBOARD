package org.udsm.udsm_hackathon2026.dto;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeographicalMetricsDto {
    private String country;
    private String region;
    private String city;
    private Long count;
}