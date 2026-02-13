package org.udsm.udsm_hackathon2026.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.udsm.udsm_hackathon2026.model.Metric;
import java.util.List;

@Repository
public interface MetricRepository extends JpaRepository<Metric, Long> {

    /*
     * OJS metrics.assoc_type values:
     *   256  = submission (abstract page view) → READERSHIP
     *   515  = galley (file download)          → DOWNLOAD
     *
     * metrics.metric = the count for each row
     * metrics.country_id = ISO 2-letter country code
     * metrics.submission_id = the article
     * metrics.day = date string like '20260213'
     */

    // ── Total downloads: SUM(metric) where assoc_type = 515 ──
    @Query(value = "SELECT COALESCE(SUM(m.metric), 0) FROM metrics m WHERE m.assoc_type = 515",
            nativeQuery = true)
    long sumTotalDownloads();

    @Query(value = "SELECT COALESCE(SUM(m.metric), 0) FROM metrics m WHERE m.assoc_type = 1048585",
            nativeQuery = true)
    long sumTotalReaders();

    // ── Top readership countries ──
    @Query(value = "SELECT COALESCE(NULLIF(m.country_id, ''), 'Unknown') AS country, " +
            "SUM(m.metric) AS total " +
            "FROM metrics m " +
            "WHERE m.assoc_type = 256 AND m.country_id IS NOT NULL " +
            "GROUP BY m.country_id " +
            "ORDER BY total DESC " +
            "LIMIT :limit",
            nativeQuery = true)
    List<Object[]> findTopReadershipCountries(@Param("limit") int limit);

    // ── Top download countries ──
    @Query(value = "SELECT COALESCE(NULLIF(m.country_id, ''), 'Unknown') AS country, " +
            "SUM(m.metric) AS total " +
            "FROM metrics m " +
            "WHERE m.assoc_type = 515 AND m.country_id IS NOT NULL " +
            "GROUP BY m.country_id " +
            "ORDER BY total DESC " +
            "LIMIT :limit",
            nativeQuery = true)
    List<Object[]> findTopDownloadCountries(@Param("limit") int limit);

    // ── Top read articles (by submission_id) ──
    @Query(value = "SELECT m.submission_id, SUM(m.metric) AS total " +
            "FROM metrics m " +
            "WHERE m.assoc_type = 1048585 AND m.submission_id IS NOT NULL " +
            "GROUP BY m.submission_id " +
            "ORDER BY total DESC " +
            "LIMIT :limit",
            nativeQuery = true)
    List<Object[]> findTopReadArticles(@Param("limit") int limit);

    // ── Aggregate by country for a date range (readership or downloads) ──
    @Query(value = "SELECT COALESCE(NULLIF(m.country_id, ''), 'Unknown') AS country, " +
            "SUM(m.metric) AS total " +
            "FROM metrics m " +
            "WHERE m.assoc_type = :assocType " +
            "AND m.day BETWEEN :fromDay AND :toDay " +
            "AND (:submissionId IS NULL OR m.submission_id = :submissionId) " +
            "GROUP BY m.country_id " +
            "ORDER BY total DESC",
            nativeQuery = true)
    List<Object[]> aggregateByCountryAndDateRange(@Param("assocType") long assocType,
                                                  @Param("fromDay") String fromDay,
                                                  @Param("toDay") String toDay,
                                                  @Param("submissionId") Long submissionId);

    // ══════════════════════════════════════════════════════════════
    // ARTICLE-SPECIFIC QUERIES (by submission_id)
    // ══════════════════════════════════════════════════════════════

    /**
     * Get total downloads for a specific article
     * assoc_type = 515 typically represents file downloads in OJS
     */
    @Query(value = "SELECT COALESCE(SUM(m.metric), 0) " +
            "FROM metrics m " +
            "WHERE m.submission_id = :submissionId " +
            "AND m.assoc_type = 515",
            nativeQuery = true)
    long getTotalDownloadsByArticle(@Param("submissionId") Long submissionId);

    /**
     * Get total reads/views for a specific article
     * assoc_type = 1048585 represents article views
     */
    @Query(value = "SELECT COALESCE(SUM(m.metric), 0) " +
            "FROM metrics m " +
            "WHERE m.submission_id = :submissionId " +
            "AND m.assoc_type = 1048585",
            nativeQuery = true)
    long getTotalReadsByArticle(@Param("submissionId") Long submissionId);

    /**
     * Get total readers (unique views) for a specific article
     * For simplicity, using the sum of metrics as proxy for total readers
     */
    @Query(value = "SELECT COALESCE(SUM(m.metric), 0) " +
            "FROM metrics m " +
            "WHERE m.submission_id = :submissionId " +
            "AND m.assoc_type = 1048585",
            nativeQuery = true)
    long getTotalReadersByArticle(@Param("submissionId") Long submissionId);

    /**
     * Get geographical distribution of reads for a specific article
     */
    @Query(value = "SELECT " +
            "COALESCE(NULLIF(m.country_id, ''), 'Unknown') AS country, " +
            "COALESCE(NULLIF(m.region, ''), 'Unknown') AS region, " +
            "COALESCE(NULLIF(m.city, ''), 'Unknown') AS city, " +
            "SUM(m.metric) AS total " +
            "FROM metrics m " +
            "WHERE m.submission_id = :submissionId " +
            "AND m.assoc_type = 1048585 " +
            "GROUP BY m.country_id, m.region, m.city " +
            "ORDER BY total DESC",
            nativeQuery = true)
    List<Object[]> getGeographicalReadsByArticle(@Param("submissionId") Long submissionId);

    /**
     * Get geographical distribution of downloads for a specific article
     */
    @Query(value = "SELECT " +
            "COALESCE(NULLIF(m.country_id, ''), 'Unknown') AS country, " +
            "COALESCE(NULLIF(m.region, ''), 'Unknown') AS region, " +
            "COALESCE(NULLIF(m.city, ''), 'Unknown') AS city, " +
            "SUM(m.metric) AS total " +
            "FROM metrics m " +
            "WHERE m.submission_id = :submissionId " +
            "AND m.assoc_type = 515 " +
            "GROUP BY m.country_id, m.region, m.city " +
            "ORDER BY total DESC",
            nativeQuery = true)
    List<Object[]> getGeographicalDownloadsByArticle(@Param("submissionId") Long submissionId);

    // ══════════════════════════════════════════════════════════════
    // ENHANCED: MONTHLY VIEWS AND DOWNLOADS COMBINED
    // ══════════════════════════════════════════════════════════════

    /**
     * Get monthly views AND downloads statistics for an article
     * Returns data grouped by month for comprehensive time-series visualization
     */
    @Query(value = "SELECT " +
            "COALESCE(views.month, downloads.month) AS month, " +
            "CAST(SUBSTRING(COALESCE(views.month, downloads.month), 1, 4) AS UNSIGNED) AS year, " +
            "CAST(SUBSTRING(COALESCE(views.month, downloads.month), 5, 2) AS UNSIGNED) AS month_num, " +
            "COALESCE(views.total_views, 0) AS views, " +
            "COALESCE(downloads.total_downloads, 0) AS downloads " +
            "FROM ( " +
            "    SELECT m.month, SUM(m.metric) AS total_views " +
            "    FROM metrics m " +
            "    WHERE m.submission_id = :submissionId " +
            "    AND m.assoc_type = 1048585 " +
            "    AND m.month IS NOT NULL " +
            "    GROUP BY m.month " +
            ") views " +
            "LEFT JOIN ( " +
            "    SELECT m.month, SUM(m.metric) AS total_downloads " +
            "    FROM metrics m " +
            "    WHERE m.submission_id = :submissionId " +
            "    AND m.assoc_type = 515 " +
            "    AND m.month IS NOT NULL " +
            "    GROUP BY m.month " +
            ") downloads ON views.month = downloads.month " +
            "UNION " +
            "SELECT " +
            "COALESCE(views.month, downloads.month) AS month, " +
            "CAST(SUBSTRING(COALESCE(views.month, downloads.month), 1, 4) AS UNSIGNED) AS year, " +
            "CAST(SUBSTRING(COALESCE(views.month, downloads.month), 5, 2) AS UNSIGNED) AS month_num, " +
            "COALESCE(views.total_views, 0) AS views, " +
            "COALESCE(downloads.total_downloads, 0) AS downloads " +
            "FROM ( " +
            "    SELECT m.month, SUM(m.metric) AS total_downloads " +
            "    FROM metrics m " +
            "    WHERE m.submission_id = :submissionId " +
            "    AND m.assoc_type = 515 " +
            "    AND m.month IS NOT NULL " +
            "    GROUP BY m.month " +
            ") downloads " +
            "LEFT JOIN ( " +
            "    SELECT m.month, SUM(m.metric) AS total_views " +
            "    FROM metrics m " +
            "    WHERE m.submission_id = :submissionId " +
            "    AND m.assoc_type = 1048585 " +
            "    AND m.month IS NOT NULL " +
            "    GROUP BY m.month " +
            ") views ON downloads.month = views.month " +
            "WHERE views.month IS NULL " +
            "ORDER BY month ASC",
            nativeQuery = true)
    List<Object[]> getMonthlyMetricsByArticle(@Param("submissionId") Long submissionId);

    @Query(value = """
        SELECT m.submission_id, SUM(m.metric) AS total
        FROM metrics m
        WHERE m.assoc_type = 515
          AND m.submission_id IS NOT NULL
        GROUP BY m.submission_id
        ORDER BY total DESC
        LIMIT :limit
        """,
            nativeQuery = true)
    List<Object[]> findTopDownloadedArticles(@Param("limit") int limit);
}