package org.udsm.udsm_hackathon2026.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Enhanced geographical metrics DTO with article metadata
 * Includes country, region, city, count, and article information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnhnacedGeographicalMetricsDto {
    private String countryCode;
    private String countryName;
    private String region;
    private String city;
    private Long count;

    // Article metadata
    private Long articleId;
    private String articleTitle;
    private List<String> authors;

    // Coordinates for mapping
    private Double latitude;
    private Double longitude;

    // Percentage of total
    private Double percentage;

}
