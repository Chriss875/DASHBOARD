package org.udsm.udsm_hackathon2026.Controller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.udsm.udsm_hackathon2026.dto.CountryCountDto;
import org.udsm.udsm_hackathon2026.dto.realtime.GeoPointDto;
import org.udsm.udsm_hackathon2026.dto.realtime.StatsDto;
import org.udsm.udsm_hackathon2026.repository.LiveEventRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/realtime")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Real-Time Analytics", description = "REST endpoints for real-time analytics dashboard with geo and time filtering")
@CrossOrigin(origins = "*")
public class RealtimeAnalyticsController {

    private final LiveEventRepository liveEventRepository;

    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @GetMapping("/geo/reads")
    @Operation(
        summary = "Get geographical distribution of reads",
        description = "Returns geo points with read counts for heatmap visualization. Supports filtering by article, date range."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Geographical distribution retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = GeoPointDto.class))
            )
        )
    })
    public ResponseEntity<List<GeoPointDto>> getReadsGeoDistribution(
            @Parameter(description = "Filter by article ID")
            @RequestParam(required = false) Long articleId,
            
            @Parameter(description = "Start date (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            
            @Parameter(description = "End date (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        String fromDay = from != null ? from.format(DAY_FORMATTER) : null;
        String toDay = to != null ? to.format(DAY_FORMATTER) : null;

        List<Object[]> results;
        if (articleId != null) {
            results = liveEventRepository.findGeoDistributionByArticle(1048585L, articleId, fromDay, toDay);
        } else {
            results = liveEventRepository.findGeoDistribution(1048585L, fromDay, toDay);
        }

        List<GeoPointDto> geoPoints = new ArrayList<>();
        for (Object[] row : results) {
            geoPoints.add(GeoPointDto.builder()
                    .countryCode((String) row[0])
                    .city((String) row[1])
                    .count(((Number) row[2]).longValue())
                    .latitude(row.length > 3 && row[3] != null ? ((Number) row[3]).doubleValue() : 0.0)
                    .longitude(row.length > 4 && row[4] != null ? ((Number) row[4]).doubleValue() : 0.0)
                    .build());
        }

        return ResponseEntity.ok(geoPoints);
    }

    @GetMapping("/geo/downloads")
    @Operation(
        summary = "Get geographical distribution of downloads",
        description = "Returns geo points with download counts for heatmap visualization. Supports filtering by article, date range."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Geographical distribution retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = GeoPointDto.class))
            )
        )
    })
    public ResponseEntity<List<GeoPointDto>> getDownloadsGeoDistribution(
            @Parameter(description = "Filter by article ID")
            @RequestParam(required = false) Long articleId,
            
            @Parameter(description = "Start date (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            
            @Parameter(description = "End date (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        String fromDay = from != null ? from.format(DAY_FORMATTER) : null;
        String toDay = to != null ? to.format(DAY_FORMATTER) : null;

        List<Object[]> results;
        if (articleId != null) {
            results = liveEventRepository.findGeoDistributionByArticle(515L, articleId, fromDay, toDay);
        } else {
            results = liveEventRepository.findGeoDistribution(515L, fromDay, toDay);
        }

        List<GeoPointDto> geoPoints = new ArrayList<>();
        for (Object[] row : results) {
            geoPoints.add(GeoPointDto.builder()
                    .countryCode((String) row[0])
                    .city((String) row[1])
                    .count(((Number) row[2]).longValue())
                    .latitude(row.length > 3 && row[3] != null ? ((Number) row[3]).doubleValue() : 0.0)
                    .longitude(row.length > 4 && row[4] != null ? ((Number) row[4]).doubleValue() : 0.0)
                    .build());
        }

        return ResponseEntity.ok(geoPoints);
    }

    @GetMapping("/by-country/reads")
    @Operation(
        summary = "Get reads aggregated by country",
        description = "Returns top countries by read count with optional date filtering"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Country aggregation retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = CountryCountDto.class))
            )
        )
    })
    public ResponseEntity<List<CountryCountDto>> getReadsByCountry(
            @Parameter(description = "Start date (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            
            @Parameter(description = "End date (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            
            @Parameter(description = "Limit number of results")
            @RequestParam(defaultValue = "50") int limit) {

        String fromDay = from != null ? from.format(DAY_FORMATTER) : null;
        String toDay = to != null ? to.format(DAY_FORMATTER) : null;

        List<Object[]> results = liveEventRepository.findByCountry(1048585L, fromDay, toDay, limit);

        List<CountryCountDto> countryData = new ArrayList<>();
        for (Object[] row : results) {
            countryData.add(new CountryCountDto(
                    (String) row[0],
                    ((Number) row[1]).longValue()
            ));
        }

        return ResponseEntity.ok(countryData);
    }

    @GetMapping("/by-country/downloads")
    @Operation(
        summary = "Get downloads aggregated by country",
        description = "Returns top countries by download count with optional date filtering"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Country aggregation retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = CountryCountDto.class))
            )
        )
    })
    public ResponseEntity<List<CountryCountDto>> getDownloadsByCountry(
            @Parameter(description = "Start date (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            
            @Parameter(description = "End date (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            
            @Parameter(description = "Limit number of results")
            @RequestParam(defaultValue = "50") int limit) {

        String fromDay = from != null ? from.format(DAY_FORMATTER) : null;
        String toDay = to != null ? to.format(DAY_FORMATTER) : null;

        List<Object[]> results = liveEventRepository.findByCountry(515L, fromDay, toDay, limit);

        List<CountryCountDto> countryData = new ArrayList<>();
        for (Object[] row : results) {
            countryData.add(new CountryCountDto(
                    (String) row[0],
                    ((Number) row[1]).longValue()
            ));
        }

        return ResponseEntity.ok(countryData);
    }

    @GetMapping("/top-articles/reads")
    @Operation(summary = "Get top articles by read count")
    @ApiResponse(responseCode = "200", description = "Top articles retrieved successfully")
    public ResponseEntity<List<Map<String, Object>>> getTopArticlesByReads(
            @Parameter(description = "Limit number of results")
            @RequestParam(defaultValue = "10") int limit) {

        List<Object[]> results = liveEventRepository.findTopArticles(1048585L, limit);

        List<Map<String, Object>> topArticles = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> article = new HashMap<>();
            article.put("articleId", ((Number) row[0]).longValue());
            article.put("count", ((Number) row[1]).longValue());
            topArticles.add(article);
        }

        return ResponseEntity.ok(topArticles);
    }

    @GetMapping("/top-articles/downloads")
    @Operation(summary = "Get top articles by download count")
    @ApiResponse(responseCode = "200", description = "Top articles retrieved successfully")
    public ResponseEntity<List<Map<String, Object>>> getTopArticlesByDownloads(
            @Parameter(description = "Limit number of results")
            @RequestParam(defaultValue = "10") int limit) {

        List<Object[]> results = liveEventRepository.findTopArticles(515L, limit);

        List<Map<String, Object>> topArticles = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> article = new HashMap<>();
            article.put("articleId", ((Number) row[0]).longValue());
            article.put("count", ((Number) row[1]).longValue());
            topArticles.add(article);
        }

        return ResponseEntity.ok(topArticles);
    }

    @GetMapping("/stats/summary")
    @Operation(
        summary = "Get overall statistics summary",
        description = "Returns aggregated stats including total reads, downloads, unique countries, etc."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Statistics summary retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = StatsDto.class)
            )
        )
    })
    public ResponseEntity<StatsDto> getStatsSummary() {
        Long totalReads = liveEventRepository.getTotalCount(1048585L);
        Long totalDownloads = liveEventRepository.getTotalCount(515L);
        
        String fiveMinutesAgo = LocalDate.now().minusDays(1).format(DAY_FORMATTER);
        Long readsLast5Min = liveEventRepository.getCountSince(1048585L, fiveMinutesAgo);
        Long downloadsLast5Min = liveEventRepository.getCountSince(515L, fiveMinutesAgo);
        
        Long uniqueCountries = liveEventRepository.countUniqueCountries();
        Long uniqueIPs = liveEventRepository.countUniqueLocations();

        StatsDto stats = StatsDto.builder()
                .totalReads(totalReads != null ? totalReads : 0L)
                .totalDownloads(totalDownloads != null ? totalDownloads : 0L)
                .readsLast5Min(readsLast5Min != null ? readsLast5Min : 0L)
                .downloadsLast5Min(downloadsLast5Min != null ? downloadsLast5Min : 0L)
                .uniqueCountries(uniqueCountries != null ? uniqueCountries : 0L)
                .uniqueIPs(uniqueIPs != null ? uniqueIPs : 0L)
                .build();

        return ResponseEntity.ok(stats);
    }
}
