package org.udsm.udsm_hackathon2026.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.udsm.udsm_hackathon2026.model.LiveEvent;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LiveEventRepository extends JpaRepository<LiveEvent, String> {

    /**
     * Get geographical distribution for a specific event type
     * @param assocType 1048585 for READ, 515 for DOWNLOAD
     * @param fromDate Start date (optional)
     * @param toDate End date (optional)
     */
    @Query(value = """
        SELECT country_id as countryCode, city, 
               COUNT(*) as count,
               AVG(CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(city, ',', -1), ',', 1) AS DECIMAL(10,6))) as latitude,
               AVG(CAST(SUBSTRING_INDEX(city, ',', -1) AS DECIMAL(10,6))) as longitude
        FROM metrics
        WHERE assoc_type = :assocType
        AND (:fromDay IS NULL OR day >= :fromDay)
        AND (:toDay IS NULL OR day <= :toDay)
        AND country_id IS NOT NULL
        GROUP BY country_id, city
        """, nativeQuery = true)
    List<Object[]> findGeoDistribution(@Param("assocType") Long assocType, 
                                        @Param("fromDay") String fromDay, 
                                        @Param("toDay") String toDay);

    /**
     * Get geographical distribution for a specific article
     */
    @Query(value = """
        SELECT country_id as countryCode, city, 
               COUNT(*) as count
        FROM metrics
        WHERE assoc_type = :assocType
        AND submission_id = :submissionId
        AND (:fromDay IS NULL OR day >= :fromDay)
        AND (:toDay IS NULL OR day <= :toDay)
        AND country_id IS NOT NULL
        GROUP BY country_id, city
        """, nativeQuery = true)
    List<Object[]> findGeoDistributionByArticle(@Param("assocType") Long assocType,
                                                 @Param("submissionId") Long submissionId,
                                                 @Param("fromDay") String fromDay,
                                                 @Param("toDay") String toDay);

    /**
     * Get country-level aggregation
     */
    @Query(value = """
        SELECT country_id, COUNT(*) as count
        FROM metrics
        WHERE assoc_type = :assocType
        AND (:fromDay IS NULL OR day >= :fromDay)
        AND (:toDay IS NULL OR day <= :toDay)
        AND country_id IS NOT NULL
        GROUP BY country_id
        ORDER BY count DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findByCountry(@Param("assocType") Long assocType,
                                  @Param("fromDay") String fromDay,
                                  @Param("toDay") String toDay,
                                  @Param("limit") int limit);

    /**
     * Get total count for an event type
     */
    @Query(value = """
        SELECT SUM(metric) 
        FROM metrics
        WHERE assoc_type = :assocType
        """, nativeQuery = true)
    Long getTotalCount(@Param("assocType") Long assocType);

    /**
     * Get count for last N minutes
     */
    @Query(value = """
        SELECT SUM(metric)
        FROM metrics
        WHERE assoc_type = :assocType
        AND day >= :fromDay
        """, nativeQuery = true)
    Long getCountSince(@Param("assocType") Long assocType, @Param("fromDay") String fromDay);

    /**
     * Count unique countries
     */
    @Query(value = """
        SELECT COUNT(DISTINCT country_id)
        FROM metrics
        WHERE country_id IS NOT NULL
        """, nativeQuery = true)
    Long countUniqueCountries();

    /**
     * Count unique IPs (approximation using city+country as proxy)
     */
    @Query(value = """
        SELECT COUNT(DISTINCT CONCAT(COALESCE(country_id, ''), COALESCE(city, '')))
        FROM metrics
        WHERE country_id IS NOT NULL
        """, nativeQuery = true)
    Long countUniqueLocations();

    /**
     * Get top articles by event type
     */
    @Query(value = """
        SELECT submission_id, SUM(metric) as count
        FROM metrics
        WHERE assoc_type = :assocType
        AND submission_id IS NOT NULL
        GROUP BY submission_id
        ORDER BY count DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findTopArticles(@Param("assocType") Long assocType, @Param("limit") int limit);
}
