package org.udsm.udsm_hackathon2026.Controller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.udsm.udsm_hackathon2026.dto.ArticleStatusDto;
import org.udsm.udsm_hackathon2026.service.ArticleStatisticsService;


@RestController
@RequestMapping("/api/v1/articles/statuses")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Article Statuses", description = "Endpoints for retrieving article statuses")
public class ArticleStatusController {
    private final ArticleStatisticsService articleStatisticsService;

    /*@GetMapping("/published")
    @Operation(
            summary = "Get published articles count",
            description = "Returns the total number of published articles (status=3) along with total submitted articles. " +
                         "The response includes totalPublished and totalSubmitted fields, while other fields will be 0."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", 
                    description = "Successfully retrieved published articles count",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ArticleStatusDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500", 
                    description = "Internal server error",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<ArticleStatusDto> getPublishedArticlesCount() {
        log.info("REST request to get published articles count");

        try {
            ArticleStatusDto response = articleStatisticsService.getPublishedArticlesCount();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching published articles count", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GetMapping("/declined")
    @Operation(
            summary = "Get declined articles count",
            description = "Returns the total number of declined articles (status=4) along with total submitted articles. " +
                         "The response includes totalDeclined and totalSubmitted fields, while other fields will be 0."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", 
                    description = "Successfully retrieved declined articles count",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ArticleStatusDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500", 
                    description = "Internal server error",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<ArticleStatusDto> getDeclinedArticlesCount() {
        log.info("REST request to get declined articles count");

        try {
            ArticleStatusDto response = articleStatisticsService.getDeclinedArticlesCount();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching declined articles count", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/under-review")
    @Operation(
            summary = "Get articles under review count",
            description = "Returns the total number of articles under review (status=1) along with total submitted articles. " +
                         "The response includes totalUnderReview and totalSubmitted fields, while other fields will be 0."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", 
                    description = "Successfully retrieved under review articles count",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ArticleStatusDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500", 
                    description = "Internal server error",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<ArticleStatusDto> getUnderReviewArticlesCount() {
        log.info("REST request to get under review articles count");

        try {
            ArticleStatusDto response = articleStatisticsService.getUnderReviewArticlesCount();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching under review articles count", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @GetMapping("/submitted")
    @Operation(
            summary = "Get total submitted articles count",
            description = "Returns the total number of all submitted articles regardless of status. " +
                         "The response includes only totalSubmitted field, while other fields will be 0."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", 
                    description = "Successfully retrieved total submitted articles count",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ArticleStatusDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500", 
                    description = "Internal server error",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<ArticleStatusDto> getTotalSubmittedArticlesCount() {
        log.info("REST request to get total submitted articles count");

        try {
            ArticleStatusDto response = articleStatisticsService.getTotalSubmittedArticlesCount();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching total submitted articles count", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }*/

    @GetMapping("/all")
    @Operation(
            summary = "Get all article statistics",
            description = "Returns comprehensive article statistics including all counts: published (status=3), " +
                         "declined (status=4), under review (status=1), and total submitted articles. " +
                         "All fields in the response will contain actual values."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", 
                    description = "Successfully retrieved all statistics",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ArticleStatusDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500", 
                    description = "Internal server error",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<ArticleStatusDto> getAllStatistics() {
        log.info("REST request to get all article statistics");

        try {
            ArticleStatusDto publishedStats = articleStatisticsService.getPublishedArticlesCount();
            ArticleStatusDto declinedStats = articleStatisticsService.getDeclinedArticlesCount();
            ArticleStatusDto underReviewStats = articleStatisticsService.getUnderReviewArticlesCount();

            // Combine all statistics
            ArticleStatusDto response = new ArticleStatusDto(
                    publishedStats.getTotalPublished(),
                    publishedStats.getTotalSubmitted(),
                    underReviewStats.getTotalUnderReview(),
                    declinedStats.getTotalDeclined()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching all statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
