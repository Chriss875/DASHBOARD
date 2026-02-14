package org.udsm.udsm_hackathon2026.service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.udsm.udsm_hackathon2026.dto.ArticleInfoDto;
import org.udsm.udsm_hackathon2026.dto.CountryCountDto;
import org.udsm.udsm_hackathon2026.dto.MonthlyMetricsDto;
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

    private static final long ASSOC_TYPE_SUBMISSION = 256L;
    private static final long ASSOC_TYPE_GALLEY = 515L;

    public long getTotalDownloads() {
        return metricRepository.sumTotalDownloads();
    }

    public long getTotalCitations() {
        return citationRepository.countTotalCitations();
    }


    public long getTotalReaders() {
        return metricRepository.sumTotalReaders();
    }


    public long getTotalArticles() {
        return publicationRepository.count();
    }

    public List<CountryCountDto> getTopReadershipCountries(int limit) {
        List<Object[]> rows = metricRepository.findTopReadershipCountries(limit);
        return toCountryCountList(rows);
    }


    public List<CountryCountDto> getTopDownloadCountries(int limit) {
        List<Object[]> rows = metricRepository.findTopDownloadCountries(limit);
        return toCountryCountList(rows);
    }


    public List<TopArticleDto> getTopReadArticles(int limit) {
        List<Object[]> rows = metricRepository.findTopReadArticles(limit);

        if (rows.isEmpty()) {
            return List.of();
        }

        List<TopArticleDto> result = new ArrayList<>();
        for (Object[] row : rows) {
            Long submissionId = toLong(row[0]);
            Long reads = toLong(row[1]);

            ArticleInfoDto articleInfo = articleService.getArticleInfo(submissionId);

            String authorsString = articleInfo.getAuthors() != null && !articleInfo.getAuthors().isEmpty()
                ? String.join(", ", articleInfo.getAuthors())
                : "Unknown";
            
            result.add(TopArticleDto.builder()
                    .articleId(submissionId)
                    .title(articleInfo.getTitle() != null ? articleInfo.getTitle() : "Untitled")
                    .authors(authorsString)
                    .articleAbstract(articleInfo.getArticleAbstract())
                    .reads(reads)
                    .build());
        }
        
        return result;
    }

    private List<CountryCountDto> toCountryCountList(List<Object[]> rows) {
        return rows.stream()
                .map(row -> CountryCountDto.builder()
                        .country((String) row[0])
                        .count(toLong(row[1]))
                        .build())
                .toList();
    }

    private Map<Long, String> fetchTitlesBySubmissionIds(List<Long> submissionIds) {
        List<Object[]> titleRows = publicationSettingRepository.findTitlesBySubmissionIds(submissionIds);
        return titleRows.stream()
                .collect(Collectors.toMap(
                        row -> toLong(row[0]),
                        row -> row[1] != null ? (String) row[1] : "Untitled",
                        (existing, replacement) -> existing // keep first locale
                ));
    }

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
    public List<MonthlyMetricsDto> getTotalMonthlyMetrics(Integer year) {
        log.info("Fetching total monthly views and downloads for all articles for year: {}", year);

        List<Object[]> rows;
        if (year != null) {
            rows = metricRepository.getTotalMonthlyMetricsByYear(year);
        } else {
            rows = metricRepository.getTotalMonthlyMetrics();
        }
        
        List<MonthlyMetricsDto> monthlyMetrics = new ArrayList<>();

        for (Object[] row : rows) {
            String month = (String) row[0];
            Integer yearValue = ((Number) row[1]).intValue();
            Integer monthNum = ((Number) row[2]).intValue();
            Long views = ((Number) row[3]).longValue();
            Long downloads = ((Number) row[4]).longValue();

            monthlyMetrics.add(new MonthlyMetricsDto(month, yearValue, monthNum, views, downloads));
        }

        log.info("Found {} months of total data across all articles (year: {})", monthlyMetrics.size(), year);
        return monthlyMetrics;
    }

    public List<MonthlyMetricsDto> getTotalMonthlyMetrics() {
        return getTotalMonthlyMetrics(null);
    }
}