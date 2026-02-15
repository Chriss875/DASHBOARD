package org.udsm.udsm_hackathon2026.controller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.udsm.udsm_hackathon2026.dto.author.*;
import org.udsm.udsm_hackathon2026.service.AuthorMetricsService;

/**
 * REST Controller for Author Metrics and Dashboard
 * Provides comprehensive analytics and insights for authors
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/authors")
@RequiredArgsConstructor
@Tag(name = "Author Metrics", description = "Endpoints for author dashboard, productivity, and performance metrics")
@CrossOrigin(origins = "*")
public class AuthorMetricsController {
    
    private final AuthorMetricsService authorMetricsService;
    
    /**
     * Endpoint #1: Get author dashboard summary
     * Returns overall statistics including submissions, publications, and acceptance rate
     */
    @GetMapping("/{authorEmail}/dashboard")
    @Operation(
        summary = "Get Author Dashboard",
        description = "Retrieve comprehensive dashboard statistics for an author including total submissions, published articles, articles under review, declined submissions, and acceptance rate"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved dashboard",
            content = @Content(schema = @Schema(implementation = AuthorDashboardDTO.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Author not found"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error"
        )
    })
    public ResponseEntity<AuthorDashboardDTO> getAuthorDashboard(
            @Parameter(description = "Author's email address", example = "john.doe@example.com", required = true)
            @PathVariable String authorEmail) {
        try {
            log.info("GET /api/v1/authors/{}/dashboard", authorEmail);
            AuthorDashboardDTO dashboard = authorMetricsService.getAuthorDashboard(authorEmail);
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            log.error("Error fetching dashboard for author: {}", authorEmail, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Endpoint #2: Get review time statistics
     * Returns average, fastest, and longest review times, plus current submissions in review
     */
    @GetMapping("/{authorEmail}/review-times")
    @Operation(
        summary = "Get Review Time Statistics",
        description = "Retrieve detailed review time metrics including average review days, fastest and longest review times, and list of submissions currently under review with days pending"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved review time statistics",
            content = @Content(schema = @Schema(implementation = ReviewTimeDTO.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Author not found"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error"
        )
    })
    public ResponseEntity<ReviewTimeDTO> getReviewTimes(
            @Parameter(description = "Author's email address", example = "john.doe@example.com", required = true)
            @PathVariable String authorEmail) {
        try {
            log.info("GET /api/v1/authors/{}/review-times", authorEmail);
            ReviewTimeDTO reviewTimes = authorMetricsService.getReviewTimes(authorEmail);
            return ResponseEntity.ok(reviewTimes);
        } catch (Exception e) {
            log.error("Error fetching review times for author: {}", authorEmail, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Endpoint #3: Get productivity metrics
     * Returns publishing patterns, yearly breakdown, and trends
     */
    @GetMapping("/{authorEmail}/productivity")
    @Operation(
        summary = "Get Productivity Metrics",
        description = "Analyze author's publishing patterns including articles published this year and month, average per year, yearly breakdown, and trend analysis (increasing/stable/decreasing)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved productivity metrics",
            content = @Content(schema = @Schema(implementation = ProductivityDTO.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Author not found"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error"
        )
    })
    public ResponseEntity<ProductivityDTO> getProductivity(
            @Parameter(description = "Author's email address", example = "john.doe@example.com", required = true)
            @PathVariable String authorEmail) {
        try {
            log.info("GET /api/v1/authors/{}/productivity", authorEmail);
            ProductivityDTO productivity = authorMetricsService.getProductivity(authorEmail);
            return ResponseEntity.ok(productivity);
        } catch (Exception e) {
            log.error("Error fetching productivity for author: {}", authorEmail, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Endpoint #4: Get author ranking
     * Returns rank, percentile, score, and badge based on performance
     */
    @GetMapping("/{authorEmail}/ranking")
    @Operation(
        summary = "Get Author Ranking",
        description = "Retrieve author's ranking among all authors based on performance score calculated from acceptance rate (40%), total publications (30%), and recent activity (30%). Includes badge assignment: Gold (90+), Silver (75-89), Bronze (60-74), None (<60)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved ranking information",
            content = @Content(schema = @Schema(implementation = RankingDTO.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Author not found"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error"
        )
    })
    public ResponseEntity<RankingDTO> getRanking(
            @Parameter(description = "Author's email address", example = "john.doe@example.com", required = true)
            @PathVariable String authorEmail) {
        try {
            log.info("GET /api/v1/authors/{}/ranking", authorEmail);
            RankingDTO ranking = authorMetricsService.getRanking(authorEmail);
            return ResponseEntity.ok(ranking);
        } catch (Exception e) {
            log.error("Error fetching ranking for author: {}", authorEmail, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Endpoint #5: Get submission timeline
     * Returns journey of each submission with current status and progress
     */
    @GetMapping("/{authorEmail}/timeline")
    @Operation(
        summary = "Get Submission Timeline",
        description = "Retrieve detailed timeline for all submissions showing current stage (Submission/Review/Copyediting/Production), days in current stage, progress percentage, and expected completion date"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved submission timeline",
            content = @Content(schema = @Schema(implementation = TimelineDTO.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Author not found"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error"
        )
    })
    public ResponseEntity<TimelineDTO> getTimeline(
            @Parameter(description = "Author's email address", example = "john.doe@example.com", required = true)
            @PathVariable String authorEmail) {
        try {
            log.info("GET /api/v1/authors/{}/timeline", authorEmail);
            TimelineDTO timeline = authorMetricsService.getTimeline(authorEmail);
            return ResponseEntity.ok(timeline);
        } catch (Exception e) {
            log.error("Error fetching timeline for author: {}", authorEmail, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Endpoint #6: Get complete dashboard
     * Returns all metrics combined in a single optimized API call
     */
    @GetMapping("/{authorEmail}/complete-dashboard")
    @Operation(
        summary = "Get Complete Dashboard",
        description = "Retrieve comprehensive dashboard combining all metrics in a single optimized API call. Includes dashboard summary, review times, productivity analysis, ranking information, and submission timeline. Results are cached for 5 minutes to improve performance."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved complete dashboard",
            content = @Content(schema = @Schema(implementation = CompleteDashboardDTO.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Author not found"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error"
        )
    })
    public ResponseEntity<CompleteDashboardDTO> getCompleteDashboard(
            @Parameter(description = "Author's email address", example = "john.doe@example.com", required = true)
            @PathVariable String authorEmail) {
        try {
            log.info("GET /api/v1/authors/{}/complete-dashboard", authorEmail);
            CompleteDashboardDTO completeDashboard = authorMetricsService.getCompleteDashboard(authorEmail);
            return ResponseEntity.ok(completeDashboard);
        } catch (Exception e) {
            log.error("Error fetching complete dashboard for author: {}", authorEmail, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Health check endpoint for author metrics service
     */
    @GetMapping("/health")
    @Operation(
        summary = "Health Check",
        description = "Verify that the Author Metrics API is operational"
    )
    @ApiResponse(responseCode = "200", description = "Service is healthy")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Author Metrics API is running");
    }
}
