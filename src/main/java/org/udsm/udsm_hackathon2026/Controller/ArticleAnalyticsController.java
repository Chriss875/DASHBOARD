package org.udsm.udsm_hackathon2026.Controller;
import org.udsm.udsm_hackathon2026.dto.ArticleListDto;
import org.udsm.udsm_hackathon2026.dto.ArticleMetricsResponseDto;
import org.udsm.udsm_hackathon2026.dto.GeographicalMetricsDto;
import org.udsm.udsm_hackathon2026.dto.MonthlyMetricsDto;
import org.udsm.udsm_hackathon2026.service.ArticleAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/api/v1/articles")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Article Analytics", description = "Endpoints for retrieving article-specific analytics")
@CrossOrigin(origins = "*")
public class ArticleAnalyticsController {

    private final ArticleAnalyticsService articleService;

    // ══════════════════════════════════════════════════════════════
    // ENDPOINT 1: Get all articles for listing and filtering
    // ══════════════════════════════════════════════════════════════

    @GetMapping
    @Operation(
            summary = "Get all articles",
            description = "Fetch all articles with ID and name (title). " +
                         "Use this endpoint for initial load and populating filter dropdowns in the frontend."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", 
                    description = "Successfully retrieved articles",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ArticleListDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500", 
                    description = "Internal server error",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<List<ArticleListDto>> getAllArticles() {
        log.info("GET /api/v1/articles - Fetching all articles for listing");
        List<ArticleListDto> articles = articleService.getAllArticlesForListing();
        return ResponseEntity.ok(articles);
    }

    // ══════════════════════════════════════════════════════════════
    // ENDPOINT 2: Get complete article metrics
    // ══════════════════════════════════════════════════════════════

    @GetMapping("/{articleId}/metrics")
    @Operation(
            summary = "Get complete article metrics",
            description = "Get comprehensive metrics for a specific article including: " +
                         "basic info (name, abstract, authors, publication date) and " +
                         "key metrics (total downloads, citations, readers)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", 
                    description = "Successfully retrieved article metrics",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ArticleMetricsResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404", 
                    description = "Article not found",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500", 
                    description = "Internal server error",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<ArticleMetricsResponseDto> getArticleMetrics(
            @Parameter(description = "Unique article identifier", required = true, example = "12345")
            @PathVariable Long articleId) {
        log.info("GET /api/v1/articles/{}/metrics - Fetching complete metrics", articleId);
        ArticleMetricsResponseDto metrics = articleService.getArticleMetrics(articleId);
        return ResponseEntity.ok(metrics);
    }

    // ══════════════════════════════════════════════════════════════
    // ENDPOINT 3: Get geographical reads distribution
    // ══════════════════════════════════════════════════════════════

    @GetMapping("/totalreads/geographicalwise/{articleId}")
    @Operation(
            summary = "Get geographical distribution of reads",
            description = "Retrieve the geographical breakdown (country, region, city) of reads for a specific article. " +
                         "Useful for creating geographical heatmaps and understanding global readership patterns."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", 
                    description = "Successfully retrieved geographical reads",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GeographicalMetricsDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404", 
                    description = "Article not found",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500", 
                    description = "Internal server error",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<List<GeographicalMetricsDto>> getGeographicalReads(
            @Parameter(description = "Unique article identifier", required = true, example = "12345")
            @PathVariable Long articleId) {
        log.info("GET /api/v1/articles/totalreads/geographicalwise/{} - Fetching geographical reads", articleId);
        List<GeographicalMetricsDto> geographicalReads = articleService.getGeographicalReads(articleId);
        return ResponseEntity.ok(geographicalReads);
    }

    // ══════════════════════════════════════════════════════════════
    // ENDPOINT 4: Get geographical downloads distribution
    // ══════════════════════════════════════════════════════════════

    @GetMapping("/totaldownloads/geographicwise/{articleId}")
    @Operation(
            summary = "Get geographical distribution of downloads",
            description = "Retrieve the geographical breakdown (country, region, city) of downloads for a specific article. " +
                         "Useful for creating geographical heatmaps and understanding global download patterns."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", 
                    description = "Successfully retrieved geographical downloads",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GeographicalMetricsDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404", 
                    description = "Article not found",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500", 
                    description = "Internal server error",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<List<GeographicalMetricsDto>> getGeographicalDownloads(
            @Parameter(description = "Unique article identifier", required = true, example = "12345")
            @PathVariable Long articleId) {
        log.info("GET /api/v1/articles/totaldownloads/geographicwise/{} - Fetching geographical downloads", articleId);
        List<GeographicalMetricsDto> geographicalDownloads = articleService.getGeographicalDownloads(articleId);
        return ResponseEntity.ok(geographicalDownloads);
    }

    /**
     * ENHANCED: Returns BOTH views AND downloads by month
     */
    @GetMapping("/{articleId}/metrics/monthly")
    @Operation(
            summary = "Get monthly views and downloads",
            description = "Retrieve monthly statistics including BOTH views and downloads for a specific article. " +
                         "Perfect for creating comprehensive time-series graphs and trend analysis. " +
                         "Optionally filter by year to get data for a specific year only."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", 
                    description = "Successfully retrieved monthly metrics",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MonthlyMetricsDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404", 
                    description = "Article not found",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500", 
                    description = "Internal server error",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<List<MonthlyMetricsDto>> getMonthlyMetrics(
            @Parameter(description = "Unique article identifier", required = true, example = "12345")
            @PathVariable Long articleId,
            @Parameter(description = "Filter by year (optional). If provided, returns only data for that year", required = false, example = "2024")
            @RequestParam(required = false) Integer year) {
        log.info("GET /api/v1/articles/{}/metrics/monthly?year={}", articleId, year);
        return ResponseEntity.ok(articleService.getMonthlyMetrics(articleId, year));
    }
}