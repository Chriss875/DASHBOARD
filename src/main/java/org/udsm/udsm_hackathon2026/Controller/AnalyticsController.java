package org.udsm.udsm_hackathon2026.Controller;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.udsm.udsm_hackathon2026.dto.CountryCountDto;
import org.udsm.udsm_hackathon2026.dto.TopArticleDto;
import org.udsm.udsm_hackathon2026.service.AnalyticsService;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    // 1. GET /api/v1/total-downloads/articles
    @GetMapping("/total-downloads/articles")
    public ResponseEntity<Map<String, Long>> getTotalDownloads() {
        return ResponseEntity.ok(Map.of("totalDownloads", analyticsService.getTotalDownloads()));
    }

    // 2. GET /api/v1/total-citations/articles
    @GetMapping("/total-citations/articles")
    public ResponseEntity<Map<String, Long>> getTotalCitations() {
        return ResponseEntity.ok(Map.of("totalCitations", analyticsService.getTotalCitations()));
    }

    // 3. GET /api/v1/total-readers/articles
    @GetMapping("/total-readers/articles")
    public ResponseEntity<Map<String, Long>> getTotalReaders() {
        return ResponseEntity.ok(Map.of("totalReaders", analyticsService.getTotalReaders()));
    }

    // 4. GET /api/v1/total/articles
    @GetMapping("/total/articles")
    public ResponseEntity<Map<String, Long>> getTotalArticles() {
        return ResponseEntity.ok(Map.of("totalArticles", analyticsService.getTotalArticles()));
    }

    // 5. GET /api/v1/top/readership/countries?limit=10
    @GetMapping("/top/readership/countries")
    public ResponseEntity<List<CountryCountDto>> getTopReadershipCountries(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(analyticsService.getTopReadershipCountries(limit));
    }

    // 6. GET /api/v1/top/downloads/countries?limit=10
    @GetMapping("/top/downloads/countries")
    public ResponseEntity<List<CountryCountDto>> getTopDownloadCountries(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(analyticsService.getTopDownloadCountries(limit));
    }

    // 7. GET /api/v1/top-read/articles?limit=10
    @GetMapping("/top-read/articles")
    public ResponseEntity<List<TopArticleDto>> getTopReadArticles(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(analyticsService.getTopReadArticles(limit));
    }
}