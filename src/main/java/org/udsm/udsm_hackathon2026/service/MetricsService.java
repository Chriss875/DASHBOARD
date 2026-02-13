package org.udsm.udsm_hackathon2026.service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.udsm.udsm_hackathon2026.dto.ArticleInfoDto;
import org.udsm.udsm_hackathon2026.dto.WebSocketPayload;
import org.udsm.udsm_hackathon2026.publisher.WebSocketPublisher;
import org.udsm.udsm_hackathon2026.repository.MetricRepository;
import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetricsService {

    private final StringRedisTemplate redisTemplate;
    private final WebSocketPublisher webSocketPublisher;
    private final MetricRepository metricRepository;
    private final ArticleInfoService articleInfoService;

    // ────────────────────────── REDIS KEY HELPERS ──────────────────────────

    private String readershipGeoKey(Long articleId) {
        return "article:" + articleId + ":readership:geo";
    }

    private String downloadsGeoKey(Long articleId) {
        return "article:" + articleId + ":downloads:geo";
    }

    // ══════════════════════════════════════════════════════════════════
    //  1️⃣  LIVE READERSHIP TRACKING
    // ══════════════════════════════════════════════════════════════════

    public void recordReadership(Long articleId, String countryCode) {
        // Atomic Redis increment for live counter
        redisTemplate.opsForHash().increment(readershipGeoKey(articleId), countryCode, 1);

        // Fetch full geo map + article info, push via WebSocket
        Map<String, Long> geoData = getReadershipGeo(articleId);
        ArticleInfoDto articleInfo = articleInfoService.getArticleInfo(articleId);

        webSocketPublisher.publish(
                "/topic/live/readership",
                WebSocketPayload.builder()
                        .articleId(articleId)
                        .type("readership")
                        .data(geoData)
                        .article(articleInfo)
                        .build()
        );
    }

    public Map<String, Long> getReadershipGeo(Long articleId) {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(readershipGeoKey(articleId));
        return toLongMap(entries);
    }

    public Map<String, Long> getReadershipGeoAll() {
        return scanAndMergeGeo("*:readership:geo");
    }

    // ══════════════════════════════════════════════════════════════════
    //  2️⃣  LIVE DOWNLOAD TRACKING
    // ══════════════════════════════════════════════════════════════════

    public void recordDownload(Long articleId, String countryCode) {
        redisTemplate.opsForHash().increment(downloadsGeoKey(articleId), countryCode, 1);

        Map<String, Long> geoData = getDownloadsGeo(articleId);
        ArticleInfoDto articleInfo = articleInfoService.getArticleInfo(articleId);

        webSocketPublisher.publish(
                "/topic/live/downloads",
                WebSocketPayload.builder()
                        .articleId(articleId)
                        .type("download")
                        .data(geoData)
                        .article(articleInfo)
                        .build()
        );
    }

    public Map<String, Long> getDownloadsGeo(Long articleId) {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(downloadsGeoKey(articleId));
        return toLongMap(entries);
    }

    public Map<String, Long> getDownloadsGeoAll() {
        return scanAndMergeGeo("*:downloads:geo");
    }

    // ══════════════════════════════════════════════════════════════════
    //  📊  DATE-RANGE AGGREGATION (from OJS metrics table)
    // ══════════════════════════════════════════════════════════════════

    /**
     * Aggregates from the OJS metrics table by country for a date range.
     *
     * @param submissionId nullable — if null, aggregates across ALL articles
     * @param eventType    "READERSHIP" or "DOWNLOAD"
     * @param fromDay      OJS day format: "20260101"
     * @param toDay        OJS day format: "20260212"
     */
    public Map<String, Long> aggregateByDateRange(Long submissionId, String eventType,
                                                  String fromDay, String toDay) {
        long assocType = "DOWNLOAD".equals(eventType) ? 515L : 256L;
        List<Object[]> rows = metricRepository.aggregateByCountryAndDateRange(
                assocType, fromDay, toDay, submissionId);

        Map<String, Long> result = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String country = (String) row[0];
            long total = toLong(row[1]);
            result.put(country, total);
        }
        return result;
    }

    // ────────────────────────── UTILS ──────────────────────────

    private Map<String, Long> toLongMap(Map<Object, Object> raw) {
        Map<String, Long> result = new HashMap<>();
        raw.forEach((k, v) -> result.put(k.toString(), Long.parseLong(v.toString())));
        return result;
    }

    private Map<String, Long> scanAndMergeGeo(String pattern) {
        Map<String, Long> merged = new HashMap<>();
        Set<String> keys = redisTemplate.keys("article:" + pattern);
        if (keys != null) {
            for (String key : keys) {
                Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
                entries.forEach((k, v) ->
                        merged.merge(k.toString(), Long.parseLong(v.toString()), Long::sum));
            }
        }
        return merged;
    }

    private long toLong(Object value) {
        if (value == null) return 0L;
        if (value instanceof Long l) return l;
        if (value instanceof BigDecimal bd) return bd.longValue();
        return ((Number) value).longValue();
    }
}