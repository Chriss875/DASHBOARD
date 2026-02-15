package org.udsm.udsm_hackathon2026.service;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.udsm.udsm_hackathon2026.dto.*;
import org.udsm.udsm_hackathon2026.repository.ArticleRepository;
import org.udsm.udsm_hackathon2026.repository.CitationRepository;
import org.udsm.udsm_hackathon2026.repository.MetricRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleAnalyticsService {
    private final ArticleRepository articleRepository;
    private final MetricRepository metricRepository;
    private final CitationRepository citationRepository;
    private final CrossrefCitationService citationService;
    private final StringRedisTemplate stringRedisTemplate; // Simple String-based Redis
    private final CountryCoordinatesService coordinatesService;
    private final ObjectMapper objectMapper; // For JSON serialization

    private static final String ARTICLE_METADATA_KEY = "article:metadata:";
    private static final long CACHE_TTL_HOURS = 24;

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

        // Cache article metadata in Redis for future use
        cacheArticleMetadata(articleId, name, authors);

        // Get metrics
        Long totalDownloads = metricRepository.getTotalDownloadsByArticle(articleId);
        Long totalReaders = metricRepository.getTotalReadersByArticle(articleId);

        // Get citations
        Long totalCitations = 0L;
        if (publicationId != null) {
            CitationResponse citationResponse = citationService.getOrUpdateCitationCount(publicationId);
            totalCitations = citationResponse.getCitationCount() != null ? citationResponse.getCitationCount() : 0L;
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
     * Get geographical distribution of reads for an article (LEGACY - basic version)
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
     * Get geographical distribution of downloads for an article (LEGACY - basic version)
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

    /**
     * Get ENHANCED geographical distribution of reads with article metadata
     */
    public List<EnhnacedGeographicalMetricsDto> getEnhancedGeographicalReads(Long articleId) {
        log.info("Fetching enhanced geographical reads for article ID: {}", articleId);

        // Get article metadata (from cache or DB)
        ArticleMetadata metadata = getArticleMetadata(articleId);

        // Get geographical data
        List<Object[]> rows = metricRepository.getGeographicalReadsByArticle(articleId);
        List<EnhnacedGeographicalMetricsDto> metrics = new ArrayList<>();

        // Calculate total for percentage
        long total = rows.stream().mapToLong(row -> ((Number) row[3]).longValue()).sum();

        for (Object[] row : rows) {
            String countryCode = (String) row[0];
            String region = (String) row[1];
            String city = (String) row[2];
            Long count = ((Number) row[3]).longValue();

            // Get coordinates for this country
            CountryCoordinatesService.CountryCoords coords = coordinatesService.getCoordinates(countryCode);

            // Calculate percentage
            double percentage = total > 0 ? (count * 100.0 / total) : 0.0;
            percentage = Math.round(percentage * 100.0) / 100.0;

            metrics.add(EnhnacedGeographicalMetricsDto.builder()
                    .countryCode(countryCode)
                    .countryName(coords.name)
                    .region(region)
                    .city(city)
                    .count(count)
                    .articleId(articleId)
                    .articleTitle(metadata.getTitle())
                    .authors(metadata.getAuthors())
                    .latitude(coords.latitude)
                    .longitude(coords.longitude)
                    .percentage(percentage)
                    .build());
        }

        log.info("Found {} enhanced geographical read locations for article {}", metrics.size(), articleId);
        return metrics;
    }

    /**
     * Get ENHANCED geographical distribution of downloads with article metadata
     */
    public List<EnhnacedGeographicalMetricsDto> getEnhancedGeographicalDownloads(Long articleId) {
        log.info("Fetching enhanced geographical downloads for article ID: {}", articleId);

        // Get article metadata (from cache or DB)
        ArticleMetadata metadata = getArticleMetadata(articleId);

        // Get geographical data
        List<Object[]> rows = metricRepository.getGeographicalDownloadsByArticle(articleId);
        List<EnhnacedGeographicalMetricsDto> metrics = new ArrayList<>();

        // Calculate total for percentage
        long total = rows.stream().mapToLong(row -> ((Number) row[3]).longValue()).sum();

        for (Object[] row : rows) {
            String countryCode = (String) row[0];
            String region = (String) row[1];
            String city = (String) row[2];
            Long count = ((Number) row[3]).longValue();

            // Get coordinates for this country
            CountryCoordinatesService.CountryCoords coords = coordinatesService.getCoordinates(countryCode);

            // Calculate percentage
            double percentage = total > 0 ? (count * 100.0 / total) : 0.0;
            percentage = Math.round(percentage * 100.0) / 100.0;

            metrics.add(EnhnacedGeographicalMetricsDto.builder()
                    .countryCode(countryCode)
                    .countryName(coords.name)
                    .region(region)
                    .city(city)
                    .count(count)
                    .articleId(articleId)
                    .articleTitle(metadata.getTitle())
                    .authors(metadata.getAuthors())
                    .latitude(coords.latitude)
                    .longitude(coords.longitude)
                    .percentage(percentage)
                    .build());
        }

        log.info("Found {} enhanced geographical download locations for article {}", metrics.size(), articleId);
        return metrics;
    }

    /**
     * Get monthly views AND downloads - ENHANCED VERSION
     * @param articleId The article ID
     * @param year Optional year filter. If null, returns all years
     */
    public List<MonthlyMetricsDto> getMonthlyMetrics(Long articleId, Integer year) {
        log.info("Fetching monthly views and downloads for article ID: {} for year: {}", articleId, year);

        List<Object[]> rows;
        if (year != null) {
            rows = metricRepository.getMonthlyMetricsByArticleAndYear(articleId, year);
        } else {
            rows = metricRepository.getMonthlyMetricsByArticle(articleId);
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

        log.info("Found {} months of data for article {} (year: {})", monthlyMetrics.size(), articleId, year);
        return monthlyMetrics;
    }

    /**
     * Get monthly views AND downloads - LEGACY VERSION (for backward compatibility)
     */
    public List<MonthlyMetricsDto> getMonthlyMetrics(Long articleId) {
        return getMonthlyMetrics(articleId, null);
    }

    /**
     * Cache article metadata in Redis as JSON string
     */
    private void cacheArticleMetadata(Long articleId, String title, List<String> authors) {
        try {
            String key = ARTICLE_METADATA_KEY + articleId;

            // Create metadata map
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("title", title);
            metadata.put("authors", authors);

            // Serialize to JSON string
            String jsonValue = objectMapper.writeValueAsString(metadata);

            // Store in Redis
            stringRedisTemplate.opsForValue().set(key, jsonValue, CACHE_TTL_HOURS, TimeUnit.HOURS);
            log.debug("Cached metadata for article {} in Redis", articleId);

        } catch (Exception e) {
            log.warn("Failed to cache article metadata in Redis for article {}", articleId, e);
        }
    }

    /**
     * Get article metadata from Redis cache (JSON string) or database
     */
    private ArticleMetadata getArticleMetadata(Long articleId) {
        try {
            // Try to get from cache first
            String key = ARTICLE_METADATA_KEY + articleId;
            String jsonValue = stringRedisTemplate.opsForValue().get(key);

            if (jsonValue != null) {
                // Deserialize from JSON
                Map<String, Object> cached = objectMapper.readValue(
                        jsonValue,
                        new TypeReference<Map<String, Object>>() {}
                );

                log.debug("Retrieved article metadata from Redis cache for article {}", articleId);
                return new ArticleMetadata(
                        (String) cached.get("title"),
                        (List<String>) cached.get("authors")
                );
            }
        } catch (Exception e) {
            log.warn("Failed to retrieve from Redis cache, falling back to database", e);
        }

        // Cache miss - fetch from database
        log.debug("Cache miss for article {}, fetching from database", articleId);
        List<Object[]> articleRows = articleRepository.findArticleDetailsById(articleId);

        if (articleRows.isEmpty()) {
            log.error("Article with ID {} not found", articleId);
            return new ArticleMetadata("Unknown Article", new ArrayList<>());
        }

        Object[] row = articleRows.get(0);
        String title = (String) row[1];
        Long publicationId = row[4] != null ? ((Number) row[4]).longValue() : null;

        List<String> authors = new ArrayList<>();
        if (publicationId != null) {
            authors = articleRepository.findAuthorNamesByPublicationId(publicationId);
        }

        // Cache it for next time
        cacheArticleMetadata(articleId, title, authors);

        return new ArticleMetadata(title, authors);
    }

    /**
     * Inner class for article metadata
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    private static class ArticleMetadata {
        private String title;
        private List<String> authors;
    }
}