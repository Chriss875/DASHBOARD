package org.udsm.udsm_hackathon2026.controller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.udsm.udsm_hackathon2026.dto.CountryCountDto;
import org.udsm.udsm_hackathon2026.dto.MonthlyMetricsDto;
import org.udsm.udsm_hackathon2026.dto.TopArticleDto;
import org.udsm.udsm_hackathon2026.dto.TopDownloadsDto;
import org.udsm.udsm_hackathon2026.service.GeneralAnalyticsService;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Home Analytics", description = "General analytics endpoints for dashboard overview")
public class HomeAnalyticsController {

    private final GeneralAnalyticsService analyticsService;

    @Operation(
            summary = "Get total downloads",
            description = "Returns the total number of downloads across all articles in the system. " +
                         "Used for dashboard overview statistics."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", 
                    description = "Successfully retrieved total downloads",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"totalDownloads\": 15432}")
                    )
            ),
            @ApiResponse(
                    responseCode = "500", 
                    description = "Internal server error",
                    content = @Content(mediaType = "application/json")
            )
    })
    @GetMapping("/total-downloads/articles")
    public ResponseEntity<Map<String, Long>> getTotalDownloads() {
        return ResponseEntity.ok(Map.of("totalDownloads", analyticsService.getTotalDownloads()));
    }

    @Operation(
            summary = "Get total citations",
            description = "Returns the total number of citations across all articles in the system. " +
                         "Used for dashboard overview statistics."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", 
                    description = "Successfully retrieved total citations",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"totalCitations\": 2847}")
                    )
            ),
            @ApiResponse(
                    responseCode = "500", 
                    description = "Internal server error",
                    content = @Content(mediaType = "application/json")
            )
    })
    @GetMapping("/total-citations/articles")
    public ResponseEntity<Map<String, Long>> getTotalCitations() {
        return ResponseEntity.ok(Map.of("totalCitations", analyticsService.getTotalCitations()));
    }

    @Operation(
            summary = "Get total readers",
            description = "Returns the total number of readers across all articles in the system. " +
                         "Used for dashboard overview statistics."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", 
                    description = "Successfully retrieved total readers",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"totalReaders\": 45678}")
                    )
            ),
            @ApiResponse(
                    responseCode = "500", 
                    description = "Internal server error",
                    content = @Content(mediaType = "application/json")
            )
    })
    @GetMapping("/total-readers/articles")
    public ResponseEntity<Map<String, Long>> getTotalReaders() {
        return ResponseEntity.ok(Map.of("totalReaders", analyticsService.getTotalReaders()));
    }

    @Operation(
            summary = "Get total articles",
            description = "Returns the total number of articles in the system. " +
                         "Used for dashboard overview statistics."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", 
                    description = "Successfully retrieved total articles",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"totalArticles\": 1234}")
                    )
            ),
            @ApiResponse(
                    responseCode = "500", 
                    description = "Internal server error",
                    content = @Content(mediaType = "application/json")
            )
    })
    @GetMapping("/total/articles")
    public ResponseEntity<Map<String, Long>> getTotalArticles() {
        return ResponseEntity.ok(Map.of("totalArticles", analyticsService.getTotalArticles()));
    }

    @Operation(
            summary = "Get top readership countries",
            description = "Returns the top countries with the highest readership. " +
                         "Useful for geographical analytics and understanding global reach."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", 
                    description = "Successfully retrieved top readership countries",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CountryCountDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500", 
                    description = "Internal server error",
                    content = @Content(mediaType = "application/json")
            )
    })
    @GetMapping("/top/readership/countries")
    public ResponseEntity<List<CountryCountDto>> getTopReadershipCountries(
            @Parameter(description = "Maximum number of countries to return", example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(analyticsService.getTopReadershipCountries(limit));
    }

    @Operation(
            summary = "Get top download countries",
            description = "Returns the top countries with the highest downloads. " +
                         "Useful for geographical analytics and understanding download patterns."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", 
                    description = "Successfully retrieved top download countries",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CountryCountDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500", 
                    description = "Internal server error",
                    content = @Content(mediaType = "application/json")
            )
    })
    @GetMapping("/top/downloads/countries")
    public ResponseEntity<List<CountryCountDto>> getTopDownloadCountries(
            @Parameter(description = "Maximum number of countries to return", example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(analyticsService.getTopDownloadCountries(limit));
    }

    @Operation(
            summary = "Get top read articles",
            description = "Returns the top articles with the highest readership. " +
                         "Includes article ID, title, authors, and read count for dashboard display."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", 
                    description = "Successfully retrieved top read articles",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TopArticleDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500", 
                    description = "Internal server error",
                    content = @Content(mediaType = "application/json")
            )
    })
    @GetMapping("/top-read/articles")
    public ResponseEntity<List<TopArticleDto>> getTopReadArticles(
            @Parameter(description = "Maximum number of articles to return", example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(analyticsService.getTopReadArticles(limit));
    }

    @Operation(
            summary = "Get top download articles",
            description = "Returns the top articles with the highest downloads. " +
                         "Includes article ID, title, authors, and download count for dashboard display."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", 
                    description = "Successfully retrieved top download articles",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TopDownloadsDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500", 
                    description = "Internal server error",
                    content = @Content(mediaType = "application/json")
            )
    })
    @GetMapping("/top-download/articles")
    public ResponseEntity<List<TopDownloadsDto>> getTopDownloadedArticles(
            @Parameter(description = "Maximum number of articles to return", example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(analyticsService.getTopDownloadedArticles(limit));
    }

    @Operation(
            summary = "Get total monthly views and downloads",
            description = "Returns total monthly statistics including BOTH views and downloads across ALL articles in the system. " +
                         "Perfect for creating comprehensive dashboard time-series graphs showing overall platform usage trends. " +
                         "Optionally filter by year to get data for a specific year only."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", 
                    description = "Successfully retrieved total monthly metrics",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MonthlyMetricsDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500", 
                    description = "Internal server error",
                    content = @Content(mediaType = "application/json")
            )
    })
    @GetMapping("/metrics/monthly")
    public ResponseEntity<List<MonthlyMetricsDto>> getTotalMonthlyMetrics(
            @Parameter(description = "Filter by year (optional). If provided, returns only data for that year across all articles", required = false, example = "2024")
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(analyticsService.getTotalMonthlyMetrics(year));
    }
}