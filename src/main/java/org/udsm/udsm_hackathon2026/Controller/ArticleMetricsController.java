/*package org.udsm.udsm_hackathon2026.Controller;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.udsm.udsm_hackathon2026.service.MetricsService;
import java.util.Map;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleMetricsController {

    private final MetricsService metricsService;

    // ──────────── LIVE READERSHIP GEO ────────────

    @GetMapping("/{id}/geo")
    public ResponseEntity<Map<String, Long>> getReadershipGeo(@PathVariable Long id) {
        return ResponseEntity.ok(metricsService.getReadershipGeo(id));
    }

    @GetMapping("/geo/all")
    public ResponseEntity<Map<String, Long>> getAllReadershipGeo() {
        return ResponseEntity.ok(metricsService.getReadershipGeoAll());
    }

    // ──────────── LIVE DOWNLOADS GEO ────────────

    @GetMapping("/{id}/downloads/geo")
    public ResponseEntity<Map<String, Long>> getDownloadsGeo(@PathVariable Long id) {
        return ResponseEntity.ok(metricsService.getDownloadsGeo(id));
    }

    @GetMapping("/downloads/geo/all")
    public ResponseEntity<Map<String, Long>> getAllDownloadsGeo() {
        return ResponseEntity.ok(metricsService.getDownloadsGeoAll());
    }

    // ──────────── DATE-RANGE AGGREGATED METRICS ────────────

    /**
     * GET /api/articles/{id}/downloads/range?from=YYYYMMDD&to=YYYYMMDD
     * Aggregates downloads for a specific article within a date range.

    @GetMapping("/{id}/downloads/range")
    public ResponseEntity<Map<String, Long>> getDownloadsByDateRange(
            @PathVariable Long id,
            @RequestParam String from,
            @RequestParam String to) {
        return ResponseEntity.ok(metricsService.aggregateByDateRange(id, "DOWNLOAD", from, to));
    }

    /**
     * GET /api/articles/{id}/readership/range?from=YYYYMMDD&to=YYYYMMDD
     * Aggregates readership for a specific article within a date range.

    @GetMapping("/{id}/readership/range")
    public ResponseEntity<Map<String, Long>> getReadershipByDateRange(
            @PathVariable Long id,
            @RequestParam String from,
            @RequestParam String to) {
        return ResponseEntity.ok(metricsService.aggregateByDateRange(id, "READERSHIP", from, to));
    }
}
*/