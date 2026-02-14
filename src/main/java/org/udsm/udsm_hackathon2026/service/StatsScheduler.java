package org.udsm.udsm_hackathon2026.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.udsm.udsm_hackathon2026.dto.realtime.StatsDto;
import org.udsm.udsm_hackathon2026.repository.LiveEventRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsScheduler {

    private final LiveEventRepository liveEventRepository;
    private final SimpMessagingTemplate messagingTemplate;

    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * Broadcast aggregated stats every 5 seconds to /topic/stats
     */
    @Scheduled(fixedDelay = 5000)
    public void broadcastStats() {
        try {
            // Calculate stats
            Long totalReads = liveEventRepository.getTotalCount(1048585L);
            Long totalDownloads = liveEventRepository.getTotalCount(515L);
            
            // Last 5 minutes (approximate using day filter)
            String fiveMinutesAgo = LocalDateTime.now().minusMinutes(5).format(DAY_FORMATTER);
            Long readsLast5Min = liveEventRepository.getCountSince(1048585L, fiveMinutesAgo);
            Long downloadsLast5Min = liveEventRepository.getCountSince(515L, fiveMinutesAgo);
            
            Long uniqueCountries = liveEventRepository.countUniqueCountries();
            Long uniqueIPs = liveEventRepository.countUniqueLocations();

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
