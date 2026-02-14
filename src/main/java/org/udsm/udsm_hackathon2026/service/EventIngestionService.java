package org.udsm.udsm_hackathon2026.service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.udsm.udsm_hackathon2026.Controller.RealtimeWebSocketController;
import org.udsm.udsm_hackathon2026.dto.realtime.EnrichedEventDto;
import org.udsm.udsm_hackathon2026.dto.realtime.EventIngestionDto;
import org.udsm.udsm_hackathon2026.model.Metric;
import org.udsm.udsm_hackathon2026.repository.MetricRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventIngestionService {

    private final GeoIPService geoIPService;
    private final MetricRepository metricRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    private final RealtimeWebSocketController realtimeWebSocketController;

    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    /**
     * Process incoming event: enrich with geo data, persist async, broadcast via WebSocket
     * This method returns immediately (202 Accepted pattern)
     */
    public EnrichedEventDto processEvent(EventIngestionDto eventDto) {
        log.debug("Processing event: type={}, articleId={}, ip={}", 
                  eventDto.getEventType(), eventDto.getArticleId(), eventDto.getIp());

        // 1. Synchronous GeoIP resolution (fast, local lookup)
        GeoIPService.GeoLocation geoLocation = geoIPService.resolveIP(eventDto.getIp());

        // 2. Create enriched event DTO
        EnrichedEventDto enrichedEvent = buildEnrichedEvent(eventDto, geoLocation);

        // 3. Async persistence (non-blocking) to existing metrics table
        persistEventAsync(eventDto, geoLocation);

        // 4. Immediate WebSocket broadcast
        broadcastEvent(enrichedEvent);

        return enrichedEvent;
    }

    private EnrichedEventDto buildEnrichedEvent(EventIngestionDto eventDto, GeoIPService.GeoLocation geoLocation) {
        Instant timestamp;
        try {
            timestamp = Instant.parse(eventDto.getTimestamp());
        } catch (Exception e) {
            timestamp = Instant.now();
        }

        String authorsJson = null;
        if (eventDto.getAuthors() != null && !eventDto.getAuthors().isEmpty()) {
            try {
                authorsJson = objectMapper.writeValueAsString(eventDto.getAuthors());
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize authors", e);
            }
        }

        return EnrichedEventDto.builder()
                .eventType(eventDto.getEventType())
                .timestamp(timestamp)
                .ip(eventDto.getIp())
                .userAgent(eventDto.getUserAgent())
                .journalPath(eventDto.getJournalPath())
                .journalTitle(eventDto.getJournalTitle())
                .articleId(eventDto.getArticleId())
                .articleTitle(eventDto.getArticleTitle())
                .doi(eventDto.getDoi())
                .sectionTitle(eventDto.getSectionTitle())
                .authorsJson(authorsJson)
                .galleyLabel(eventDto.getGalley() != null ? eventDto.getGalley().getGalleyLabel() : null)
                .galleyMimeType(eventDto.getGalley() != null ? eventDto.getGalley().getMimeType() : null)
                .galleyFileName(eventDto.getGalley() != null ? eventDto.getGalley().getFileName() : null)
                .country(geoLocation.getCountry())
                .countryCode(geoLocation.getCountryCode())
                .city(geoLocation.getCity())
                .continent(geoLocation.getContinent())
                .latitude(geoLocation.getLatitude())
                .longitude(geoLocation.getLongitude())
                .build();
    }

    @Async
    protected void persistEventAsync(EventIngestionDto eventDto, GeoIPService.GeoLocation geoLocation) {
        try {
            LocalDateTime timestamp;
            try {
                timestamp = LocalDateTime.ofInstant(Instant.parse(eventDto.getTimestamp()), ZoneId.systemDefault());
            } catch (Exception e) {
                timestamp = LocalDateTime.now();
            }

            // Determine assoc_type: 1048585 = READ, 515 = DOWNLOAD
            Long assocType = "READ".equalsIgnoreCase(eventDto.getEventType()) ? 1048585L : 515L;

            // Insert into existing metrics table
            Metric metric = Metric.builder()
                    .loadId(UUID.randomUUID().toString())
                    .contextId(1L) // Default context, adjust as needed
                    .submissionId(eventDto.getArticleId())
                    .assocId(eventDto.getArticleId())
                    .assocType(assocType)
                    .day(timestamp.format(DAY_FORMATTER))
                    .month(timestamp.format(MONTH_FORMATTER))
                    .countryId(geoLocation.getCountryCode())
                    .region(geoLocation.getRegion())
                    .city(geoLocation.getCity())
                    .metricType("ojs::counter")
                    .metric(1) // Increment by 1
                    .build();

            metricRepository.save(metric);
            log.debug("Event persisted to metrics table: loadId={}, articleId={}, country={}", 
                     metric.getLoadId(), metric.getSubmissionId(), metric.getCountryId());

            // After successful persistence, broadcast updated geo distribution to subscribed clients
            realtimeWebSocketController.broadcastRealtimeEvent(
                eventDto.getArticleId(), 
                eventDto.getEventType(), 
                geoLocation.getCountry()
            );

        } catch (Exception e) {
            log.error("Failed to persist event asynchronously", e);
        }
    }

    private void broadcastEvent(EnrichedEventDto enrichedEvent) {
        try {
            String topic = "READ".equalsIgnoreCase(enrichedEvent.getEventType()) 
                    ? "/topic/reads" 
                    : "/topic/downloads";
            
            messagingTemplate.convertAndSend(topic, enrichedEvent);
            log.debug("Event broadcast to {} successfully", topic);

        } catch (Exception e) {
            log.error("Failed to broadcast event via WebSocket", e);
        }
    }
}
