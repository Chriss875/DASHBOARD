package org.udsm.udsm_hackathon2026.Controller;
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
import org.udsm.udsm_hackathon2026.dto.realtime.EnrichedEventDto;
import org.udsm.udsm_hackathon2026.dto.realtime.EventIngestionDto;
import org.udsm.udsm_hackathon2026.service.EventIngestionService;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Event Ingestion", description = "Real-time OJS event ingestion from PHP plugin")
@CrossOrigin(origins = "*")
public class EventIngestionController {

    private final EventIngestionService eventIngestionService;

    @PostMapping("/ingest")
    @Operation(
        summary = "Ingest OJS read/download event",
        description = "Accepts real-time events from OJS plugin, enriches with geo data, persists asynchronously, and broadcasts via WebSocket."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "202",
            description = "Event accepted and processing started",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = EnrichedEventDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request - Invalid event payload",
            content = @Content
        )
    })
    public ResponseEntity<?> ingestEvent(
            @Parameter(description = "Event payload from OJS plugin", required = true)
            @RequestBody EventIngestionDto eventDto) {

        // Basic validation
        if (eventDto.getEventType() == null || eventDto.getArticleId() == null) {
            log.warn("Invalid event payload: eventType={}, articleId={}", 
                     eventDto.getEventType(), eventDto.getArticleId());
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("eventType and articleId are required"));
        }

        // Process event (enriches, persists async, broadcasts)
        try {
            EnrichedEventDto enrichedEvent = eventIngestionService.processEvent(eventDto);
            
            log.info("Event ingested successfully: type={}, articleId={}, country={}", 
                     enrichedEvent.getEventType(), 
                     enrichedEvent.getArticleId(), 
                     enrichedEvent.getCountry());

            // 202 Accepted - processing in progress
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(enrichedEvent);

        } catch (Exception e) {
            log.error("Error processing event", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to process event: " + e.getMessage()));
        }
    }

    @GetMapping("/health")
    @Operation(summary = "Health check for ingestion endpoint")
    @ApiResponse(responseCode = "200", description = "Service is healthy")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Ingestion service is running");
    }

    // Simple error response class
    private record ErrorResponse(String error) {}
}
