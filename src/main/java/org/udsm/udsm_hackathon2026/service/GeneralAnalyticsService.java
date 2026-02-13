package org.udsm.udsm_hackathon2026.service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.udsm.udsm_hackathon2026.dto.ArticleInfoDto;
import org.udsm.udsm_hackathon2026.dto.CountryCountDto;
import org.udsm.udsm_hackathon2026.dto.TopArticleDto;
import org.udsm.udsm_hackathon2026.dto.TopDownloadsDto;
import org.udsm.udsm_hackathon2026.repository.CitationRepository;
import org.udsm.udsm_hackathon2026.repository.MetricRepository;
import org.udsm.udsm_hackathon2026.repository.PublicationRepository;
import org.udsm.udsm_hackathon2026.repository.PublicationSettingRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeneralAnalyticsService {

    private final MetricRepository metricRepository;
    private final CitationRepository citationRepository;
    private final PublicationRepository publicationRepository;
    private final ArticleInfoService articleService;
    private final PublicationSettingRepository publicationSettingRepository;

    /**
     * OJS assoc_type constants from the metrics table.
     */
    private static final long ASSOC_TYPE_SUBMISSION = 256L;  // abstract page view = readership
    private static final long ASSOC_TYPE_GALLEY = 515L;      // file view = download

    // ══════════════════════════════════════════════════════════════
    //  1. Total Downloads — SUM(metric) WHERE assoc_type = 515
    // ══════════════════════════════════════════════════════════════

    public long getTotalDownloads() {
        return metricRepository.sumTotalDownloads();
    }

    // ══════════════════════════════════════════════════════════════
    //  2. Total Citations — COUNT of rows in OJS citations table
    // ══════════════════════════════════════════════════════════════

    public long getTotalCitations() {
        return citationRepository.countTotalCitations();
    }

    // ══════════════════════════════════════════════════════════════
    //  3. Total Readers — SUM(metric) WHERE assoc_type = 256
    // ══════════════════════════════════════════════════════════════

    public long getTotalReaders() {
        return metricRepository.sumTotalReaders();
    }

    // ══════════════════════════════════════════════════════════════
    //  4. Total Articles — COUNT of publications
    // ══════════════════════════════════════════════════════════════

    public long getTotalArticles() {
        return publicationRepository.count();
    }

    // ══════════════════════════════════════════════════════════════
    //  5. Top Readership Countries
    // ══════════════════════════════════════════════════════════════

    public List<CountryCountDto> getTopReadershipCountries(int limit) {
        List<Object[]> rows = metricRepository.findTopReadershipCountries(limit);
        return toCountryCountList(rows);
    }

    // ══════════════════════════════════════════════════════════════
    //  6. Top Download Countries
    // ══════════════════════════════════════════════════════════════

    public List<CountryCountDto> getTopDownloadCountries(int limit) {
        List<Object[]> rows = metricRepository.findTopDownloadCountries(limit);
        return toCountryCountList(rows);
    }

    // ══════════════════════════════════════════════════════════════
    //  7. Top Read Articles (with title from publication_settings)
    // ══════════════════════════════════════════════════════════════

    public List<TopArticleDto> getTopReadArticles(int limit) {
        List<Object[]> rows = metricRepository.findTopReadArticles(limit);

        if (rows.isEmpty()) {
            return List.of();

        }

        // Collect submission_ids
        List<Long> submissionIds = rows.stream()
                .map(row -> toLong(row[0]))
                .toList();

        // Batch-fetch titles: submission_id → title
        Map<Long, String> titleMap = fetchTitlesBySubmissionIds(submissionIds);

        List<TopArticleDto> result = new ArrayList<>();
        for (Object[] row : rows) {
            Long submissionId = toLong(row[0]);
            Long reads = toLong(row[1]);
            String title = titleMap.getOrDefault(submissionId, "Untitled");
            result.add(TopArticleDto.builder()
                    .articleId(submissionId)
                    .title(title)
                    .reads(reads)
                    .build());
        }
        return result;
    }

    // ────────────────────────── HELPERS ──────────────────────────

    private List<CountryCountDto> toCountryCountList(List<Object[]> rows) {
        return rows.stream()
                .map(row -> CountryCountDto.builder()
                        .country((String) row[0])
                        .count(toLong(row[1]))
                        .build())
                .toList();
    }

    /**
     * Fetches titles via the join: submissions.submission_id → publications → publication_settings.
     * Returns a map of submission_id → title.
     */
    private Map<Long, String> fetchTitlesBySubmissionIds(List<Long> submissionIds) {
        List<Object[]> titleRows = publicationSettingRepository.findTitlesBySubmissionIds(submissionIds);
        return titleRows.stream()
                .collect(Collectors.toMap(
                        row -> toLong(row[0]),
                        row -> row[1] != null ? (String) row[1] : "Untitled",
                        (existing, replacement) -> existing // keep first locale
                ));
    }

    /**
     * Native queries return BigDecimal/BigInteger for SUM results on MariaDB.
     * This safely converts to Long.
     */
    private long toLong(Object value) {
        if (value == null) return 0L;
        if (value instanceof Long l) return l;
        if (value instanceof BigDecimal bd) return bd.longValue();
        return ((Number) value).longValue();
    }

    public List<TopDownloadsDto> getTopDownloadedArticles(int limit) {
        List<Object[]> rows = metricRepository.findTopDownloadedArticles(limit);
        if (rows.isEmpty()) {
            return List.of();
        }
        return rows.stream()
                .map(row -> {
                    Long submissionId = ((Number) row[0]).longValue();
                    Long totalDownloads = ((Number) row[1]).longValue();

                    // Fetch article metadata (title + authors)
                    ArticleInfoDto article = articleService.getArticleInfo(submissionId);

                    return TopDownloadsDto.builder()
                            .articleId(submissionId)
                            .title(article.getTitle())
                            .authors(String.join("; ", article.getAuthors()))
                            .totalDownloads(totalDownloads)
                            .build();
                })
                .toList();
    }
}