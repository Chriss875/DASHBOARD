package org.udsm.udsm_hackathon2026.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.udsm.udsm_hackathon2026.model.Publication;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Repository
public interface PublicationRepository extends JpaRepository<Publication, Long> {
    /**
     * Get count of articles grouped by status
     * @return List of maps containing status and count
     */
    @Query("SELECT p.status as status, COUNT(p) as count " +
            "FROM Publication p " +
            "GROUP BY p.status " +
            "ORDER BY p.status")
    List<Map<String, Object>> getArticleCountByStatus();

    /**
     * Get count of articles by specific status
     * @param status the status code
     * @return count of articles
     */
    @Query("SELECT COUNT(p) FROM Publication p WHERE p.status = :status")
    Long getCountByStatus(@Param("status") Integer status);

    /**
     * Get count of published articles (status = 3)
     * @return count of published articles
     */
    @Query("SELECT COUNT(p) FROM Publication p WHERE p.status = 3")
    Long getPublishedCount();

    /**
     * Get count of scheduled articles (status = 5)
     * @return count of scheduled articles
     */
    @Query("SELECT COUNT(p) FROM Publication p WHERE p.status = 5")
    Long getScheduledCount();

    /**
     * Get count of articles by multiple statuses
     * @param statuses list of status codes
     * @return count of articles
     */
    @Query("SELECT COUNT(p) FROM Publication p WHERE p.status IN :statuses")
    Long getCountByStatuses(@Param("statuses") List<Integer> statuses);

    /**
     * Get all articles by status
     * @param status the status code
     * @return list of publications
     */
    List<Publication> findByStatus(Integer status);

    /**
     * Get count of all articles
     * @return total count
     */
    @Query("SELECT COUNT(p) FROM Publication p")
    Long getTotalCount();

    // Find publication by DOI
    Optional<Publication> findByDoi(String doi);

    // Find all publications with DOIs
    @Query("SELECT p FROM Publication p WHERE p.doi IS NOT NULL AND p.doi != ''")
    List<Publication> findAllWithDoi();

    // Find publications that need citation update (older than X hours)
    @Query("SELECT p FROM Publication p WHERE p.doi IS NOT NULL AND p.doi != '' " +
            "AND (p.lastCitationCheck IS NULL OR p.lastCitationCheck < :cutoffTime)")
    List<Publication> findPublicationsNeedingUpdate(LocalDateTime cutoffTime);

    // Find publications with recent citation increases
    @Query("SELECT p FROM Publication p WHERE p.citationCount > 0 ORDER BY p.lastCitationCheck DESC")
    List<Publication> findRecentlyUpdatedPublications();

}