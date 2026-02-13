package org.udsm.udsm_hackathon2026.service;
import org.udsm.udsm_hackathon2026.dto.ArticleListDto;
import org.udsm.udsm_hackathon2026.dto.ArticleMetricsResponseDto;
import org.udsm.udsm_hackathon2026.dto.GeographicalMetricsDto;
import org.udsm.udsm_hackathon2026.repository.ArticleRepository;
import org.udsm.udsm_hackathon2026.repository.CitationRepository;
import org.udsm.udsm_hackathon2026.repository.MetricRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleAnalyticsService {

    private final ArticleRepository articleRepository;
    private final MetricRepository metricRepository;
    private final CitationRepository citationRepository;

    /**
     * Get all articles for listing (ID and name only - no categories)
     */
    public List<ArticleListDto> getAllArticlesForListing() {
        log.info("Fetching all articles for listing");

        List<Object[]> rows = articleRepository.findAllArticlesForListing();
        List<ArticleListDto> articles = new ArrayList<>();

        for (Object[] row : rows) {
            Long id = ((Number) row[0]).longValue();
            String name = (String) row[1];

            articles.add(new ArticleListDto(id, name));
        }

        log.info("Found {} articles for listing", articles.size());
        return articles;
    }

    /**
     * Get complete article information with all metrics
     */
    public ArticleMetricsResponseDto getArticleMetrics(Long articleId) {
        log.info("Fetching complete metrics for article ID: {}", articleId);

        // Get article basic info
        List<Object[]> articleRows = articleRepository.findArticleDetailsById(articleId);
        if (articleRows.isEmpty()) {
            log.error("Article with ID {} not found", articleId);
            throw new RuntimeException("Article with ID " + articleId + " not found");
        }

        Object[] row = articleRows.get(0);
        Long id = ((Number) row[0]).longValue();
        String name = (String) row[1];
        String abstractText = row[2] != null ? (String) row[2] : "";
        String datePublished = row[3] != null ? row[3].toString() : null;
        Long publicationId = row[4] != null ? ((Number) row[4]).longValue() : null;

        // Get authors
        List<String> authors = new ArrayList<>();
        if (publicationId != null) {
            authors = articleRepository.findAuthorNamesByPublicationId(publicationId);
        }

        // Get metrics
        Long totalDownloads = metricRepository.getTotalDownloadsByArticle(articleId);
        Long totalReaders = metricRepository.getTotalReadersByArticle(articleId);

        // Get citations
        Long totalCitations = 0L;
        if (publicationId != null) {
            totalCitations = citationRepository.countCitationsByPublicationId(publicationId);
        }

        ArticleMetricsResponseDto response = new ArticleMetricsResponseDto(
                id,
                name,
                abstractText,
                authors,
                totalDownloads,
                totalCitations,
                totalReaders,
                datePublished
        );

        log.info("Successfully fetched metrics for article: {}", name);
        return response;
    }

    /**
     * Get geographical distribution of reads for an article
     */
    public List<GeographicalMetricsDto> getGeographicalReads(Long articleId) {
        log.info("Fetching geographical reads for article ID: {}", articleId);

        List<Object[]> rows = metricRepository.getGeographicalReadsByArticle(articleId);
        List<GeographicalMetricsDto> metrics = new ArrayList<>();

        for (Object[] row : rows) {
            String country = (String) row[0];
            String region = (String) row[1];
            String city = (String) row[2];
            Long count = ((Number) row[3]).longValue();
            metrics.add(new GeographicalMetricsDto(country, region, city, count));
        }

        log.info("Found {} geographical read locations for article {}", metrics.size(), articleId);
        return metrics;
    }

    /**
     * Get geographical distribution of downloads for an article
     */
    public List<GeographicalMetricsDto> getGeographicalDownloads(Long articleId) {
        log.info("Fetching geographical downloads for article ID: {}", articleId);

        List<Object[]> rows = metricRepository.getGeographicalDownloadsByArticle(articleId);
        List<GeographicalMetricsDto> metrics = new ArrayList<>();

        for (Object[] row : rows) {
            String country = (String) row[0];
            String region = (String) row[1];
            String city = (String) row[2];
            Long count = ((Number) row[3]).longValue();
            metrics.add(new GeographicalMetricsDto(country, region, city, count));
        }

        log.info("Found {} geographical download locations for article {}", metrics.size(), articleId);
        return metrics;
    }
}