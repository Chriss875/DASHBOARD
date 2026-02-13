package org.udsm.udsm_hackathon2026.repository;
import org.udsm.udsm_hackathon2026.model.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    /**
     * Get all published article IDs with their titles
     * Status 3 = Published in OJS
     */
    @Query(value = "SELECT s.submission_id, ps.setting_value AS title " +
            "FROM submissions s " +
            "JOIN publications p ON s.current_publication_id = p.publication_id " +
            "JOIN publication_settings ps ON p.publication_id = ps.publication_id " +
            "WHERE s.status = 3 " +
            "AND ps.setting_name = 'title' " +
            "AND ps.locale = 'en_US' " +
            "ORDER BY s.submission_id",
            nativeQuery = true)
    List<Object[]> findAllPublishedArticles();

    /**
     * Get article title by submission ID
     */
    @Query(value = "SELECT ps.setting_value " +
            "FROM submissions s " +
            "JOIN publications p ON s.current_publication_id = p.publication_id " +
            "JOIN publication_settings ps ON p.publication_id = ps.publication_id " +
            "WHERE s.submission_id = :submissionId " +
            "AND ps.setting_name = 'title' " +
            "AND ps.locale = 'en_US'",
            nativeQuery = true)
    Optional<String> findArticleTitleBySubmissionId(@Param("submissionId") Long submissionId);
}