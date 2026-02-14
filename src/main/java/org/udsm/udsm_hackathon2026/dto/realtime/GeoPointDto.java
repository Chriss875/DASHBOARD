package org.udsm.udsm_hackathon2026.dto.realtime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Geographical point with count for heatmap rendering")
public class GeoPointDto {
    
    @Schema(description = "Latitude", example = "-6.8")
    private Double latitude;
    
    @Schema(description = "Longitude", example = "39.28")
    private Double longitude;
    
    @Schema(description = "Country name", example = "Tanzania")
    private String country;
    
    @Schema(description = "Country code (ISO 2-letter)", example = "TZ")
    private String countryCode;
    
    @Schema(description = "City name", example = "Dar es Salaam")
    private String city;
    
    @Schema(description = "Count of events at this location", example = "45")
    private Long count;
}
