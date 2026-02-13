/*package org.udsm.udsm_hackathon2026.Controller;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.udsm.udsm_hackathon2026.dto.DownloadEventDto;
import org.udsm.udsm_hackathon2026.dto.ViewEventDto;
import org.udsm.udsm_hackathon2026.service.MetricsService;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/live")
@RequiredArgsConstructor
public class LiveEventController {

    private final MetricsService metricsService;

    // ══════════════════════════════════════════════════════════════
    //  POST — called by JS embedded in OJS article pages
    // ══════════════════════════════════════════════════════════════

    /**
     * POST /api/v1/live/readership
     * Body: { "articleId": 123, "country": "TZ" }

    @PostMapping("/readership")
    public ResponseEntity<Map<String, String>> recordReadership(@RequestBody ViewEventDto dto) {
        metricsService.recordReadership(dto.getArticleId(), dto.getCountry());
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    /**
     * POST /api/v1/live/downloads
     * Body: { "articleId": 123, "country": "TZ" }

    @PostMapping("/downloads")
    public ResponseEntity<Map<String, String>> recordDownload(@RequestBody DownloadEventDto dto) {
        metricsService.recordDownload(dto.getArticleId(), dto.getCountry());
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    // ══════════════════════════════════════════════════════════════
    //  GET — called by Admin Dashboard for live snapshot
    // ══════════════════════════════════════════════════════════════

    /**
     * GET /api/v1/live/readership?articleId=123  (optional)
     * If articleId omitted → returns merged geo across ALL articles.

    @GetMapping("/readership")
    public ResponseEntity<Map<String, Long>> getLiveReadership(
            @RequestParam(required = false) Long articleId) {
        if (articleId != null) {
            return ResponseEntity.ok(metricsService.getReadershipGeo(articleId));
        }
        return ResponseEntity.ok(metricsService.getReadershipGeoAll());
    }

    /**
     * GET /api/v1/live/downloads?articleId=123  (optional)

    @GetMapping("/downloads")
    public ResponseEntity<Map<String, Long>> getLiveDownloads(
            @RequestParam(required = false) Long articleId) {
        if (articleId != null) {
            return ResponseEntity.ok(metricsService.getDownloadsGeo(articleId));
        }
        return ResponseEntity.ok(metricsService.getDownloadsGeoAll());
    }
}
*/