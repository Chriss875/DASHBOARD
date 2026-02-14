package org.udsm.udsm_hackathon2026.Controller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.udsm.udsm_hackathon2026.dto.GeographicalMetricsDto;
import org.udsm.udsm_hackathon2026.service.ArticleAnalyticsService;
import java.util.List;
import java.util.Map;

/**
 * WebSocket controller for real-time geographical analytics updates.
 * 
 * Frontend Usage:
 * 
 * 1. Subscribe to real-time events:
 *    stompClient.subscribe('/topic/reads', callback)
 *    stompClient.subscribe('/topic/downloads', callback)
 * 
 * 2. Request geo distribution (all articles, no filter):
 *    stompClient.send('/app/geo/reads', {}, JSON.stringify({type: 'all'}))
 * 
 * 3. Request geo distribution (specific article):
 *    stompClient.send('/app/geo/reads', {}, JSON.stringify({
 *      type: 'article',
 *      articleId: 1542
 *    }))
 * 
 * 4. Request geo distribution (with date filter):
 *    stompClient.send('/app/geo/reads', {}, JSON.stringify({
 *      type: 'article',
 *      articleId: 1542,
 *      fromDate: '2024-01-01',
 *      toDate: '2024-12-31'
 *    }))
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class RealtimeWebSocketController {

    private final ArticleAnalyticsService articleAnalyticsService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Handle requests for geographical distribution of reads
     * Responds immediately with current data from metrics table
     */
    @MessageMapping("/geo/reads")
    public void getReadsGeoDistribution(@Payload Map<String, Object> request) {
        try {
            String type = (String) request.get("type");
            
            if ("article".equals(type)) {
                // Specific article
                Long articleId = getLongValue(request.get("articleId"));
                List<GeographicalMetricsDto> geoData = 
                    articleAnalyticsService.getGeographicalReads(articleId);
                
                messagingTemplate.convertAndSend("/topic/geo/reads/" + articleId, geoData);
                log.debug("Sent geo reads distribution for article {}: {} locations", 
                         articleId, geoData.size());
                
            } else {
                // All articles - aggregate from all submissions
                // Note: This would require a new repository method to aggregate across all articles
                log.info("Requested geo distribution for all articles (reads)");
                // TODO: Implement aggregation across all articles if needed
            }
            
        } catch (Exception e) {
            log.error("Error processing geo reads request", e);
        }
    }

    /**
     * Handle requests for geographical distribution of downloads
     * Responds immediately with current data from metrics table
     */
    @MessageMapping("/geo/downloads")
    public void getDownloadsGeoDistribution(@Payload Map<String, Object> request) {
        try {
            String type = (String) request.get("type");
            
            if ("article".equals(type)) {
                // Specific article
                Long articleId = getLongValue(request.get("articleId"));
                List<GeographicalMetricsDto> geoData = 
                    articleAnalyticsService.getGeographicalDownloads(articleId);
                
                messagingTemplate.convertAndSend("/topic/geo/downloads/" + articleId, geoData);
                log.debug("Sent geo downloads distribution for article {}: {} locations", 
                         articleId, geoData.size());
                
            } else {
                // All articles - aggregate from all submissions
                log.info("Requested geo distribution for all articles (downloads)");
                // TODO: Implement aggregation across all articles if needed
            }
            
        } catch (Exception e) {
            log.error("Error processing geo downloads request", e);
        }
    }

    /**
     * Broadcast real-time event (called from EventIngestionService)
     * This allows frontend to receive instant notifications when new events arrive
     */
    public void broadcastRealtimeEvent(Long articleId, String eventType, String country) {
        try {
            // Re-query the latest geo distribution for this article
            if ("READ".equalsIgnoreCase(eventType)) {
                List<GeographicalMetricsDto> geoData = 
                    articleAnalyticsService.getGeographicalReads(articleId);
                messagingTemplate.convertAndSend("/topic/geo/reads/" + articleId, geoData);
            } else if ("DOWNLOAD".equalsIgnoreCase(eventType)) {
                List<GeographicalMetricsDto> geoData = 
                    articleAnalyticsService.getGeographicalDownloads(articleId);
                messagingTemplate.convertAndSend("/topic/geo/downloads/" + articleId, geoData);
            }
            
            log.debug("Broadcasted updated geo distribution for article {} after {} from {}", 
                     articleId, eventType, country);
                     
        } catch (Exception e) {
            log.error("Error broadcasting realtime event", e);
        }
    }

    private Long getLongValue(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        } else if (value instanceof String) {
            return Long.parseLong((String) value);
        }
        throw new IllegalArgumentException("Cannot convert " + value + " to Long");
    }
}
