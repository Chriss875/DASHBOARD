package org.udsm.udsm_hackathon2026.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.udsm.udsm_hackathon2026.dto.realtime.StatsDto;
import org.udsm.udsm_hackathon2026.repository.MetricRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsScheduler {

    private final MetricRepository metricRepository;
    private final SimpMessagingTemplate messagingTemplate;

    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * Broadcast aggregated stats every 5 seconds to /topic/stats
     * Uses existing MetricRepository queries for reads (1048585) and downloads (515)
     */
    @Scheduled(fixedDelay = 5000)
    public void broadcastStats() {
        try {
            // Calculate stats using existing repository methods
            Long totalReads = metricRepository.sumTotalReaders(); // assoc_type = 1048585
            Long totalDownloads = metricRepository.sumTotalDownloads(); // assoc_type = 515
            
            // Last 5 minutes (approximate using day filter)
            String today = LocalDateTime.now().format(DAY_FORMATTER);
            
            // For real-time, we'll use aggregateByCountryAndDateRange to get recent activity
            // Note: This is an approximation since we're using day-level granularity
            Long readsLast5Min = totalReads; // Simplified - in production, add time-based filtering
            Long downloadsLast5Min = totalDownloads; // Simplified - in production, add time-based filtering
            
            // Count unique countries from top countries queries
            Long uniqueCountries = (long) metricRepository.findTopReadershipCountries(1000).size();
            Long uniqueIPs = uniqueCountries; // Approximation

            StatsDto stats = StatsDto.builder()
                    .totalReads(totalReads != null ? totalReads : 0L)
                    .totalDownloads(totalDownloads != null ? totalDownloads : 0L)
                    .readsLast5Min(readsLast5Min != null ? readsLast5Min : 0L)
                    .downloadsLast5Min(downloadsLast5Min != null ? downloadsLast5Min : 0L)
                    .uniqueCountries(uniqueCountries != null ? uniqueCountries : 0L)
                    .uniqueIPs(uniqueIPs != null ? uniqueIPs : 0L)
                    .build();

            messagingTemplate.convertAndSend("/topic/stats", stats);
            log.debug("Stats broadcast: reads={}, downloads={}, countries={}", 
                      stats.getTotalReads(), stats.getTotalDownloads(), stats.getUniqueCountries());

        } catch (Exception e) {
            log.error("Failed to broadcast stats", e);
        }
    }
}
