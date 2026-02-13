package org.udsm.udsm_hackathon2026.Controller;
import org.udsm.udsm_hackathon2026.dto.ArticleListDto;
import org.udsm.udsm_hackathon2026.dto.ArticleMetricsResponseDto;
import org.udsm.udsm_hackathon2026.dto.GeographicalMetricsDto;
import org.udsm.udsm_hackathon2026.service.ArticleAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
            description = "Fetch all articles with ID, name (title), and category. Use this for initial load and filter dropdown."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved articles"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
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
            description = "Get all important metrics for an article: name, category, abstract, authors, total downloads, total citations, total readers"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved article metrics"),
            @ApiResponse(responseCode = "404", description = "Article not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ArticleMetricsResponseDto> getArticleMetrics(
            @Parameter(description = "Article ID", required = true)
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
            description = "Retrieve the geographical breakdown (country, region, city) of reads for a specific article"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved geographical reads"),
            @ApiResponse(responseCode = "404", description = "Article not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<GeographicalMetricsDto>> getGeographicalReads(
            @Parameter(description = "Article ID", required = true)
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
            description = "Retrieve the geographical breakdown (country, region, city) of downloads for a specific article"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved geographical downloads"),
            @ApiResponse(responseCode = "404", description = "Article not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<GeographicalMetricsDto>> getGeographicalDownloads(
            @Parameter(description = "Article ID", required = true)
            @PathVariable Long articleId) {
        log.info("GET /api/v1/articles/totaldownloads/geographicwise/{} - Fetching geographical downloads", articleId);
        List<GeographicalMetricsDto> geographicalDownloads = articleService.getGeographicalDownloads(articleId);
        return ResponseEntity.ok(geographicalDownloads);
    }
}