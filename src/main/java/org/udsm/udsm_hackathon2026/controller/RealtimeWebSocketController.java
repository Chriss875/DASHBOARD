package org.udsm.udsm_hackathon2026.controller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.udsm.udsm_hackathon2026.dto.EnhnacedGeographicalMetricsDto;
import org.udsm.udsm_hackathon2026.service.ArticleAnalyticsService;
import java.util.List;
import java.util.Map;

/**
 * ENHANCED WebSocket controller for real-time geographical analytics with article metadata.
 *
 * Frontend Usage:
 *
 * 1. Subscribe to real-time events:
 *    stompClient.subscribe('/topic/reads', callback)
 *    stompClient.subscribe('/topic/downloads', callback)
 *
 * 2. Request ENHANCED geo distribution (specific article with metadata):
 *    stompClient.send('/app/geo/reads/enhanced', {}, JSON.stringify({
 *      articleId: 1542
 *    }))
 *
 * Response includes:
 * - countryCode, countryName
 * - region, city
 * - count, percentage
 * - articleId, articleTitle, authors[]
 * - latitude, longitude
 *
 * 3. Legacy endpoints still available:
 *    stompClient.send('/app/geo/reads', {}, JSON.stringify({type: 'article', articleId: 1542}))
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class RealtimeWebSocketController {

    private final ArticleAnalyticsService articleAnalyticsService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * ENHANCED: Handle requests for geographical distribution of reads WITH article metadata
     * Responds with enriched data including article title, authors, coordinates, and percentages
     */
    @MessageMapping("/geo/reads/enhanced")
    public void getEnhancedReadsGeoDistribution(@Payload Map<String, Object> request) {
        try {
            Long articleId = getLongValue(request.get("articleId"));

            // Get enhanced geographical data with article metadata from cache/DB
            List<EnhnacedGeographicalMetricsDto> geoData =
                    articleAnalyticsService.getEnhancedGeographicalReads(articleId);

            // Send to article-specific topic
            messagingTemplate.convertAndSend("/topic/geo/reads/enhanced/" + articleId, geoData);

            log.info("Sent enhanced geo reads for article {}: {} locations, title={}, authors={}",
                    articleId,
                    geoData.size(),
                    geoData.isEmpty() ? "N/A" : geoData.get(0).getArticleTitle(),
                    geoData.isEmpty() ? "N/A" : geoData.get(0).getAuthors());

        } catch (Exception e) {
            log.error("Error processing enhanced geo reads request", e);
        }
    }

    /**
     * ENHANCED: Handle requests for geographical distribution of downloads WITH article metadata
     * Responds with enriched data including article title, authors, coordinates, and percentages
     */
    @MessageMapping("/geo/downloads/enhanced")
    public void getEnhancedDownloadsGeoDistribution(@Payload Map<String, Object> request) {
        try {
            Long articleId = getLongValue(request.get("articleId"));

            // Get enhanced geographical data with article metadata from cache/DB
            List<EnhnacedGeographicalMetricsDto > geoData =
                    articleAnalyticsService.getEnhancedGeographicalDownloads(articleId);

            // Send to article-specific topic
            messagingTemplate.convertAndSend("/topic/geo/downloads/enhanced/" + articleId, geoData);

            log.info("Sent enhanced geo downloads for article {}: {} locations, title={}, authors={}",
                    articleId,
                    geoData.size(),
                    geoData.isEmpty() ? "N/A" : geoData.get(0).getArticleTitle(),
                    geoData.isEmpty() ? "N/A" : geoData.get(0).getAuthors());

        } catch (Exception e) {
            log.error("Error processing enhanced geo downloads request", e);
        }
    }

    /**
     * LEGACY: Handle requests for geographical distribution of reads (basic version)
     * Kept for backward compatibility
     */
    @MessageMapping("/geo/reads")
    public void getReadsGeoDistribution(@Payload Map<String, Object> request) {
        try {
            String type = (String) request.get("type");

            if ("article".equals(type)) {
                Long articleId = getLongValue(request.get("articleId"));
                var geoData = articleAnalyticsService.getGeographicalReads(articleId);

                messagingTemplate.convertAndSend("/topic/geo/reads/" + articleId, geoData);
                log.debug("Sent legacy geo reads distribution for article {}: {} locations",
                        articleId, geoData.size());
            } else {
                log.info("Requested geo distribution for all articles (reads) - not yet implemented");
            }

        } catch (Exception e) {
            log.error("Error processing geo reads request", e);
        }
    }

    /**
     * LEGACY: Handle requests for geographical distribution of downloads (basic version)
     * Kept for backward compatibility
     */
    @MessageMapping("/geo/downloads")
    public void getDownloadsGeoDistribution(@Payload Map<String, Object> request) {
        try {
            String type = (String) request.get("type");

            if ("article".equals(type)) {
                Long articleId = getLongValue(request.get("articleId"));
                var geoData = articleAnalyticsService.getGeographicalDownloads(articleId);

                messagingTemplate.convertAndSend("/topic/geo/downloads/" + articleId, geoData);
                log.debug("Sent legacy geo downloads distribution for article {}: {} locations",
                        articleId, geoData.size());
            } else {
                log.info("Requested geo distribution for all articles (downloads) - not yet implemented");
            }

        } catch (Exception e) {
            log.error("Error processing geo downloads request", e);
        }
    }

    /**
     * Broadcast real-time event with ENHANCED data (called from EventIngestionService)
     * This sends updated geographical distribution WITH article metadata after each new event
     */
    public void broadcastRealtimeEventEnhanced(Long articleId, String eventType, String country) {
        try {
            // Re-query the latest enhanced geo distribution for this article
            if ("READ".equalsIgnoreCase(eventType)) {
                List<EnhnacedGeographicalMetricsDto > geoData =
                        articleAnalyticsService.getEnhancedGeographicalReads(articleId);
                messagingTemplate.convertAndSend("/topic/geo/reads/enhanced/" + articleId, geoData);

                log.debug("Broadcasted enhanced geo reads for article {} after {} from {} - {} locations",
                        articleId, eventType, country, geoData.size());

            } else if ("DOWNLOAD".equalsIgnoreCase(eventType)) {
                List<EnhnacedGeographicalMetricsDto>  geoData =
                        articleAnalyticsService.getEnhancedGeographicalDownloads(articleId);
                messagingTemplate.convertAndSend("/topic/geo/downloads/enhanced/" + articleId, geoData);

                log.debug("Broadcasted enhanced geo downloads for article {} after {} from {} - {} locations",
                        articleId, eventType, country, geoData.size());
            }

        } catch (Exception e) {
            log.error("Error broadcasting enhanced realtime event", e);
        }
    }

    /**
     * LEGACY: Broadcast real-time event (basic version, for backward compatibility)
     */
    public void broadcastRealtimeEvent(Long articleId, String eventType, String country) {
        try {
            if ("READ".equalsIgnoreCase(eventType)) {
                var geoData = articleAnalyticsService.getGeographicalReads(articleId);
                messagingTemplate.convertAndSend("/topic/geo/reads/" + articleId, geoData);
            } else if ("DOWNLOAD".equalsIgnoreCase(eventType)) {
                var geoData = articleAnalyticsService.getGeographicalDownloads(articleId);
                messagingTemplate.convertAndSend("/topic/geo/downloads/" + articleId, geoData);
            }

            log.debug("Broadcasted legacy geo distribution for article {} after {} from {}",
                    articleId, eventType, country);

        } catch (Exception e) {
            log.error("Error broadcasting legacy realtime event", e);
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