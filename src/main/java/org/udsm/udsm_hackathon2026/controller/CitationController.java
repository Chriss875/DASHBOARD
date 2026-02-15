package org.udsm.udsm_hackathon2026.controller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.udsm.udsm_hackathon2026.dto.CitationResponse;
import org.udsm.udsm_hackathon2026.model.PublicationCitationHistory;
import org.udsm.udsm_hackathon2026.service.CrossrefCitationService;
import java.util.List;


@RestController
@RequestMapping("/api/citations")
@RequiredArgsConstructor
@Slf4j
public class CitationController {
    private final CrossrefCitationService citationService;

    /**
     * Get citation count for a specific publication
     * GET /api/citations/publication/{id}
     */
    @GetMapping("/publication/{publicationId}")
    public ResponseEntity<CitationResponse> getCitationCount(@PathVariable Long publicationId) {
        log.info("Getting citation count for publication {}", publicationId);
        CitationResponse response = citationService.getOrUpdateCitationCount(publicationId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/recent-increases")
    public ResponseEntity<List<PublicationCitationHistory>> getRecentCitationIncreases() {
        log.info("Getting recent citation increases");
        List<PublicationCitationHistory> increases = citationService.getRecentCitationIncreases();
        return ResponseEntity.ok(increases);
    }
}


        /*
         * Update citation count for a specific publication
         * POST /api/citations/publication/{id}/update

        @PostMapping("/publication/{publicationId}/update")
        public ResponseEntity<CitationResponse> updateCitationCount(@PathVariable Long publicationId) {
            log.info("Updating citation count for publication {}", publicationId);
            CitationResponse response = citationService.updateCitationCount(publicationId);
            return ResponseEntity.ok(response);
        }


         * Update citation count by DOI
         * POST /api/citations/doi/{doi}/update

        @PostMapping("/doi/{doi}/update")
        public ResponseEntity<CitationResponse> updateCitationCountByDoi(@PathVariable String doi) {
            log.info("Updating citation count for DOI: {}", doi);
            CitationResponse response = citationService.updateCitationCountByDoi(doi);
            return ResponseEntity.ok(response);
        }




         * Get citation history for a publication
         * GET /api/citations/publication/{id}/history

        @GetMapping("/publication/{publicationId}/history")
        public ResponseEntity<List<PublicationCitationHistory>> getCitationHistory(
                @PathVariable Long publicationId) {
            log.info("Getting citation history for publication {}", publicationId);
            List<PublicationCitationHistory> history = citationService.getCitationHistory(publicationId);
            return ResponseEntity.ok(history);
        }

}*/


