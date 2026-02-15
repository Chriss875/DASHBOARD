package org.udsm.udsm_hackathon2026.scheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.udsm.udsm_hackathon2026.dto.CitationResponse;
import org.udsm.udsm_hackathon2026.service.CrossrefCitationService;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CitationUpdateScheduler {
    private final CrossrefCitationService citationService;
    private boolean schedulerEnabled=true;


    @Scheduled(cron = "${citation.scheduler.cron:0 0 */6 * * *}")
    public void updateStaleCitations() {
        if (!schedulerEnabled) {
            log.debug("Citation scheduler is disabled");
            return;
        }

        log.info("Starting scheduled citation update");

        try {
            // Update publications older than 24 hours
            List<CitationResponse> responses = citationService.updateStalePublications(24);

            long successCount = responses.stream()
                    .filter(r -> "SUCCESS".equals(r.getStatus()))
                    .count();

            long errorCount = responses.stream()
                    .filter(r -> "ERROR".equals(r.getStatus()))
                    .count();

            long newCitations = responses.stream()
                    .filter(r -> r.getCitationIncrease() != null && r.getCitationIncrease() > 0)
                    .mapToInt(CitationResponse::getCitationIncrease)
                    .sum();

            log.info("Scheduled update complete: {} successful, {} errors, {} new citations detected",
                    successCount, errorCount, newCitations);

        } catch (Exception e) {
            log.error("Error in scheduled citation update", e);
        }
    }

    /**
     * Daily summary of citation changes
     * Runs at 9:00 AM every day
     */
    @Scheduled(cron = "${citation.summary.cron:0 0 9 * * *}")
    public void dailyCitationSummary() {
        if (!schedulerEnabled) {
            return;
        }

        log.info("Generating daily citation summary");

        try {
            var recentIncreases = citationService.getRecentCitationIncreases();

            if (!recentIncreases.isEmpty()) {
                int totalNewCitations = recentIncreases.stream()
                        .mapToInt(h -> h.getCitationCount() - h.getPreviousCount())
                        .sum();

                log.info("Daily Summary: {} publications received {} new citations",
                        recentIncreases.size(), totalNewCitations);
            } else {
                log.info("Daily Summary: No new citations detected");
            }

        } catch (Exception e) {
            log.error("Error generating daily citation summary", e);
        }
    }
}
