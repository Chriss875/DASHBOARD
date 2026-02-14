package org.udsm.udsm_hackathon2026.service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.udsm.udsm_hackathon2026.dto.GlobalGeoDistributionDTO;
import org.udsm.udsm_hackathon2026.dto.realtime.EnrichedEventDto;
import org.udsm.udsm_hackathon2026.dto.realtime.EventIngestionDto;
import org.udsm.udsm_hackathon2026.model.Metric;
import org.udsm.udsm_hackathon2026.repository.MetricRepository;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventIngestionService {

    private final GeoIPService geoIPService;
    private final MetricRepository metricRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    private final MetricsService metricsService;
    private final CountryCoordinatesService coordinatesService;

    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    /**
     * Process incoming event with simplified WebSocket broadcasting
     * 
     * Flow:
     * 1. Resolve GeoIP
     * 2. Increment Redis counters
     * 3. Broadcast individual event to /topic/{type}/live
     * 4. Broadcast aggregated geo distribution to /topic/{type}/geo
     * 5. Save to database (async)
     */
    public EnrichedEventDto processEvent(EventIngestionDto eventDto) {
        log.debug("Processing event: type={}, articleId={}, ip={}", 
                  eventDto.getEventType(), eventDto.getArticleId(), eventDto.getIp());

        // 1. Synchronous GeoIP resolution (fast, local lookup)
        GeoIPService.GeoLocation geoLocation = geoIPService.resolveIP(eventDto.getIp());

        // 2. Create enriched event DTO
        EnrichedEventDto enrichedEvent = buildEnrichedEvent(eventDto, geoLocation);

        // 3. Increment Redis counters (real-time, in-memory)
        incrementRedisCounters(eventDto, geoLocation);

        // 4. Broadcast to WebSocket (2 topics: live event + geo distribution)
        broadcastEvent(enrichedEvent, geoLocation);

        // 5. Async database persistence
        persistEventAsync(eventDto, geoLocation);

        return enrichedEvent;
    }
    
    /**
     * Increment Redis counters for real-time tracking
     */
    private void incrementRedisCounters(EventIngestionDto eventDto, GeoIPService.GeoLocation geoLocation) {
        try {
            String countryCode = geoLocation.getCountryCode();
            
            if ("READ".equalsIgnoreCase(eventDto.getEventType())) {
                metricsService.recordReadership(eventDto.getArticleId(), countryCode);
                log.debug("Redis: Incremented read counter for article {} in {}", 
                         eventDto.getArticleId(), countryCode);
            } else if ("DOWNLOAD".equalsIgnoreCase(eventDto.getEventType())) {
                metricsService.recordDownload(eventDto.getArticleId(), countryCode);
                log.debug("Redis: Incremented download counter for article {} in {}", 
                         eventDto.getArticleId(), countryCode);
            }
        } catch (Exception e) {
            log.error("Failed to increment Redis counters", e);
        }
    }

    private EnrichedEventDto buildEnrichedEvent(EventIngestionDto eventDto, GeoIPService.GeoLocation geoLocation) {
        Instant timestamp;
        
        // Parse timestamp from event, with robust error handling
        if (eventDto.getTimestamp() == null || eventDto.getTimestamp().isEmpty()) {
            timestamp = Instant.now();
            log.warn("No timestamp provided in event for article {}. Using server time: {}", 
                     eventDto.getArticleId(), timestamp);
        } else {
            try {
                timestamp = Instant.parse(eventDto.getTimestamp());
                log.debug("Parsed timestamp from event: {} (article {})", 
                         timestamp, eventDto.getArticleId());
            } catch (Exception e) {
                timestamp = Instant.now();
                log.error("Invalid timestamp format '{}' for article {}. Using server time: {}. Error: {}", 
                         eventDto.getTimestamp(), eventDto.getArticleId(), timestamp, e.getMessage());
            }
        }

        String authorsJson = null;
        if (eventDto.getAuthors() != null && !eventDto.getAuthors().isEmpty()) {
            try {
                authorsJson = objectMapper.writeValueAsString(eventDto.getAuthors());
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize authors", e);
            }
        }

        // Extract galley information from nested object (for downloads)
        String galleyLabel = null;
        String galleyMimeType = null;
        String galleyFileName = null;
        if (eventDto.getGalley() != null) {
            galleyLabel = eventDto.getGalley().getGalleyLabel();
            galleyMimeType = eventDto.getGalley().getMimeType();
            galleyFileName = eventDto.getGalley().getFileName();
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
                .galleyLabel(galleyLabel)
                .galleyMimeType(galleyMimeType)
                .galleyFileName(galleyFileName)
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
                    .contextId(1L)
                    .submissionId(eventDto.getArticleId())
                    .assocId(eventDto.getArticleId())
                    .assocType(assocType)
                    .day(timestamp.format(DAY_FORMATTER))
                    .month(timestamp.format(MONTH_FORMATTER))
                    .countryId(geoLocation.getCountryCode())
                    .region(geoLocation.getRegion())
                    .city(geoLocation.getCity())
                    .metricType("ojs::counter")
                    .metric(1)
                    .build();

            metricRepository.save(metric);
            log.debug("Database: Event persisted - article={}, country={}, type={}", 
                     metric.getSubmissionId(), metric.getCountryId(), eventDto.getEventType());

        } catch (Exception e) {
            log.error("Failed to persist event to database", e);
        }
    }

    /**
     * Broadcast to WebSocket - Simplified to 4 focused topics
     * 
     * For READS:
     * - /topic/reads/live → Individual event
     * - /topic/reads/geo → Aggregated global distribution
     * 
     * For DOWNLOADS:
     * - /topic/downloads/live → Individual event  
     * - /topic/downloads/geo → Aggregated global distribution
     */
    private void broadcastEvent(EnrichedEventDto enrichedEvent, GeoIPService.GeoLocation geoLocation) {
        try {
            boolean isRead = "READ".equalsIgnoreCase(enrichedEvent.getEventType());
            
            // 1. Broadcast individual live event
            String liveTopic = isRead ? "/topic/reads/live" : "/topic/downloads/live";
            messagingTemplate.convertAndSend(liveTopic, enrichedEvent);
            log.debug("WebSocket: Broadcast to {} - article={}, country={}", 
                     liveTopic, enrichedEvent.getArticleId(), geoLocation.getCountry());

            // 2. Broadcast aggregated geographical distribution (ALL articles)
            String geoTopic = isRead ? "/topic/reads/geo" : "/topic/downloads/geo";
            GlobalGeoDistributionDTO geoDistribution = buildGlobalGeoDistribution(isRead);
            messagingTemplate.convertAndSend(geoTopic, geoDistribution);
            log.debug("WebSocket: Broadcast to {} - total={} across {} countries", 
                     geoTopic, geoDistribution.getTotal(), geoDistribution.getCountryCount());

        } catch (Exception e) {
            log.error("Failed to broadcast event via WebSocket", e);
        }
    }
    
    /**
     * Build global geographical distribution from Redis
     */
    private GlobalGeoDistributionDTO buildGlobalGeoDistribution(boolean isRead) {
        // Get aggregated data from Redis
        Map<String, Long> geoData = isRead 
            ? metricsService.getReadershipGeoAll() 
            : metricsService.getDownloadsGeoAll();
        
        // Calculate total
        long total = geoData.values().stream().mapToLong(Long::longValue).sum();
        
        // Build country metrics with coordinates
        List<GlobalGeoDistributionDTO.CountryMetric> countries = new ArrayList<>();
        
        for (Map.Entry<String, Long> entry : geoData.entrySet()) {
            String countryCode = entry.getKey();
            Long count = entry.getValue();
            
            CountryCoordinatesService.CountryCoords coords = coordinatesService.getCoordinates(countryCode);
            
            double percentage = total > 0 ? (count * 100.0 / total) : 0.0;
            percentage = Math.round(percentage * 100.0) / 100.0;
            
            countries.add(GlobalGeoDistributionDTO.CountryMetric.builder()
                .countryCode(countryCode)
                .countryName(coords.name)
                .count(count)
                .latitude(coords.latitude)
                .longitude(coords.longitude)
                .percentage(percentage)
                .build());
        }
        
        // Sort by count descending
        countries.sort((a, b) -> Long.compare(b.getCount(), a.getCount()));
        
        return GlobalGeoDistributionDTO.builder()
            .type(isRead ? "reads" : "downloads")
            .timestamp(Instant.now())
            .total(total)
            .countryCount(countries.size())
            .countries(countries)
            .build();
    }
}
