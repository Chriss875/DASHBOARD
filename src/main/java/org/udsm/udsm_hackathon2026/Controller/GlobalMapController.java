package org.udsm.udsm_hackathon2026.Controller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.udsm.udsm_hackathon2026.dto.GlobalGeoDistributionDTO;
import org.udsm.udsm_hackathon2026.service.CountryCoordinatesService;
import org.udsm.udsm_hackathon2026.service.MetricsService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for global aggregated geographical analytics
 * Shows total reads/downloads across ALL articles on a world map
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/global")
@RequiredArgsConstructor
@Tag(name = "Global Map Analytics", description = "Aggregated geographical data across all articles with coordinates")
@CrossOrigin(origins = "*")
public class GlobalMapController {
    
    private final MetricsService metricsService;
    private final CountryCoordinatesService coordinatesService;
    
    /**
     * Get global geographical distribution of reads (all articles aggregated)
     * Returns enriched data with coordinates for map markers
     */
    @GetMapping("/map/reads")
    @Operation(
        summary = "Get Global Read Distribution with Coordinates",
        description = "Returns aggregated read counts by country across ALL articles with lat/lng coordinates for map visualization. Data is cached in Redis for real-time performance."
    )
    @ApiResponse(
        responseCode = "200",
        description = "Successfully retrieved global read distribution",
        content = @Content(schema = @Schema(implementation = GlobalGeoDistributionDTO.class))
    )
    public ResponseEntity<GlobalGeoDistributionDTO> getGlobalReadDistribution() {
        try {
            log.info("GET /api/v1/global/map/reads - Fetching global read distribution with coordinates");
            
            // Get aggregated data from Redis (all articles combined)
            Map<String, Long> geoData = metricsService.getReadershipGeoAll();
            
            // Calculate total
            long totalReads = geoData.values().stream().mapToLong(Long::longValue).sum();
            
            // Build enriched country metrics with coordinates
            List<GlobalGeoDistributionDTO.CountryMetric> countries = new ArrayList<>();
            
            for (Map.Entry<String, Long> entry : geoData.entrySet()) {
                String countryCode = entry.getKey();
                Long count = entry.getValue();
                
                // Get coordinates for this country
                CountryCoordinatesService.CountryCoords coords = coordinatesService.getCoordinates(countryCode);
                
                // Calculate percentage
                double percentage = totalReads > 0 ? (count * 100.0 / totalReads) : 0.0;
                percentage = Math.round(percentage * 100.0) / 100.0; // Round to 2 decimals
                
                countries.add(GlobalGeoDistributionDTO.CountryMetric.builder()
                    .countryCode(countryCode)
                    .countryName(coords.name)
                    .count(count)
                    .latitude(coords.latitude)
                    .longitude(coords.longitude)
                    .percentage(percentage)
                    .build());
            }
            
            // Sort by count (descending)
            countries.sort((a, b) -> Long.compare(b.getCount(), a.getCount()));
            
            GlobalGeoDistributionDTO response = GlobalGeoDistributionDTO.builder()
                .type("reads")
                .timestamp(java.time.Instant.now())
                .total(totalReads)
                .countryCount(countries.size())
                .countries(countries)
                .build();
            
            log.debug("Global reads: {} total across {} countries", totalReads, countries.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error fetching global read distribution", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get global geographical distribution of downloads (all articles aggregated)
     * Returns enriched data with coordinates for map markers
     */
    @GetMapping("/map/downloads")
    @Operation(
        summary = "Get Global Download Distribution with Coordinates",
        description = "Returns aggregated download counts by country across ALL articles with lat/lng coordinates for map visualization. Data is cached in Redis for real-time performance."
    )
    @ApiResponse(
        responseCode = "200",
        description = "Successfully retrieved global download distribution",
        content = @Content(schema = @Schema(implementation = GlobalGeoDistributionDTO.class))
    )
    public ResponseEntity<GlobalGeoDistributionDTO> getGlobalDownloadDistribution() {
        try {
            log.info("GET /api/v1/global/map/downloads - Fetching global download distribution with coordinates");
            
            // Get aggregated data from Redis (all articles combined)
            Map<String, Long> geoData = metricsService.getDownloadsGeoAll();
            
            // Calculate total
            long totalDownloads = geoData.values().stream().mapToLong(Long::longValue).sum();
            
            // Build enriched country metrics with coordinates
            List<GlobalGeoDistributionDTO.CountryMetric> countries = new ArrayList<>();
            
            for (Map.Entry<String, Long> entry : geoData.entrySet()) {
                String countryCode = entry.getKey();
                Long count = entry.getValue();
                
                // Get coordinates for this country
                CountryCoordinatesService.CountryCoords coords = coordinatesService.getCoordinates(countryCode);
                
                // Calculate percentage
                double percentage = totalDownloads > 0 ? (count * 100.0 / totalDownloads) : 0.0;
                percentage = Math.round(percentage * 100.0) / 100.0;
                
                countries.add(GlobalGeoDistributionDTO.CountryMetric.builder()
                    .countryCode(countryCode)
                    .countryName(coords.name)
                    .count(count)
                    .latitude(coords.latitude)
                    .longitude(coords.longitude)
                    .percentage(percentage)
                    .build());
            }
            
            // Sort by count (descending)
            countries.sort((a, b) -> Long.compare(b.getCount(), a.getCount()));
            
            GlobalGeoDistributionDTO response = GlobalGeoDistributionDTO.builder()
                .type("downloads")
                .timestamp(java.time.Instant.now())
                .total(totalDownloads)
                .countryCount(countries.size())
                .countries(countries)
                .build();
            
            log.debug("Global downloads: {} total across {} countries", totalDownloads, countries.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error fetching global download distribution", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get combined global statistics (reads + downloads)
     */
    @GetMapping("/map/combined")
    @Operation(
        summary = "Get Combined Global Distribution",
        description = "Returns both reads and downloads aggregated by country across ALL articles"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Successfully retrieved combined distribution"
    )
    public ResponseEntity<Map<String, Object>> getCombinedDistribution() {
        try {
            log.info("GET /api/v1/global/map/combined - Fetching combined distribution");
            
            Map<String, Long> reads = metricsService.getReadershipGeoAll();
            Map<String, Long> downloads = metricsService.getDownloadsGeoAll();
            
            long totalReads = reads.values().stream().mapToLong(Long::longValue).sum();
            long totalDownloads = downloads.values().stream().mapToLong(Long::longValue).sum();
            
            Map<String, Object> response = new HashMap<>();
            response.put("reads", Map.of(
                "total", totalReads,
                "countryCount", reads.size(),
                "distribution", reads
            ));
            response.put("downloads", Map.of(
                "total", totalDownloads,
                "countryCount", downloads.size(),
                "distribution", downloads
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error fetching combined distribution", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    @Operation(summary = "Health check for global map analytics")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Global map analytics API is healthy");
    }
}
