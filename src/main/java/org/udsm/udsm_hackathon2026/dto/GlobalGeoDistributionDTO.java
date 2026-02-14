package org.udsm.udsm_hackathon2026.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Global geographical distribution with coordinates for map visualization
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Global geographical distribution for all articles combined")
public class GlobalGeoDistributionDTO {
    
    @Schema(description = "Type of metric: reads or downloads", example = "reads")
    private String type;
    
    @Schema(description = "Total count across all countries", example = "15234")
    private Long total;
    
    @Schema(description = "Number of unique countries", example = "87")
    private Integer countryCount;
    
    @Schema(description = "List of countries with their geo data and counts")
    private List<CountryMetric> countries;
    
    /**
     * Individual country metric with geographical coordinates
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Country-level metrics with geographical coordinates")
    public static class CountryMetric {
        
        @Schema(description = "Country code (ISO 2-letter)", example = "TZ")
        private String countryCode;
        
        @Schema(description = "Country name", example = "Tanzania")
        private String countryName;
        
        @Schema(description = "Count of reads/downloads", example = "1789")
        private Long count;
        
        @Schema(description = "Latitude (capital city or centroid)", example = "-6.7924")
        private Double latitude;
        
        @Schema(description = "Longitude (capital city or centroid)", example = "39.2083")
        private Double longitude;
        
        @Schema(description = "Percentage of total", example = "11.74")
        private Double percentage;
    }
}
