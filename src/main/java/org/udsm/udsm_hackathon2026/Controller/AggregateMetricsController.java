/*package org.udsm.udsm_hackathon2026.Controller;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.udsm.udsm_hackathon2026.service.MetricsService;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/metrics")
@RequiredArgsConstructor
public class AggregateMetricsController {

    private final MetricsService metricsService;

    /**
     * GET /api/v1/metrics/aggregate?eventType=READERSHIP&from=20260101&to=20260212&submissionId=123
     *
     * eventType    — required: "READERSHIP" or "DOWNLOAD"
     * from         — required: OJS day format YYYYMMDD (e.g., "20260101")
     * to           — required: OJS day format YYYYMMDD (e.g., "20260212")
     * submissionId — optional: if omitted, aggregates across ALL articles
     *
     * Returns: { "TZ": 42, "US": 18, "KE": 7 }

    @GetMapping("/aggregate")
    public ResponseEntity<Map<String, Long>> aggregate(
            @RequestParam String eventType,
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam(required = false) Long submissionId) {

        Map<String, Long> result = metricsService.aggregateByDateRange(
                submissionId, eventType.toUpperCase(), from, to);
        return ResponseEntity.ok(result);
    }
}
*/

