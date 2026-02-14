package org.udsm.udsm_hackathon2026.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Country statistics data transfer object")
public class CountryCountDto {
    @Schema(description = "Country name", example = "Tanzania")
    private String country;
    
    @Schema(description = "Count of reads/downloads for this country", example = "1250")
    private Long count;
}