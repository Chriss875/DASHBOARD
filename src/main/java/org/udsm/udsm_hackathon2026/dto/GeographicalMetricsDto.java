package org.udsm.udsm_hackathon2026.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Geographical distribution metrics data transfer object")
public class GeographicalMetricsDto {
    @Schema(description = "Country name", example = "Tanzania")
    private String country;
    
    @Schema(description = "Region/state name", example = "Dar es Salaam")
    private String region;
    
    @Schema(description = "City name", example = "Dar es Salaam")
    private String city;
    
    @Schema(description = "Count of reads/downloads for this location", example = "456")
    private Long count;
}