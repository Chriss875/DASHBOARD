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

    // ── Total readers: SUM(metric) where assoc_type = 256 ──
    @Query(value = "SELECT COALESCE(SUM(m.metric), 0) FROM metrics m WHERE m.assoc_type = 256",
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
            "WHERE m.assoc_type = 256 AND m.submission_id IS NOT NULL " +
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
}