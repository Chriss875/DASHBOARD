package org.udsm.udsm_hackathon2026.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.udsm.udsm_hackathon2026.model.Author;
import java.util.List;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {
    List<Author> findByPublicationIdOrderBySeqAsc(Long publicationId);
    
    /**
     * Get all distinct authors for the authors list endpoint
     * Returns all unique authors ordered by author_id
     */
    @Query("SELECT DISTINCT a FROM Author a ORDER BY a.authorId ASC")
    List<Author> findAllDistinctAuthors();
    
    // ============= Dashboard Metrics Queries =============
    
    /**
     * Find all submission IDs for a given author email
     */
    @Query("SELECT DISTINCT s.submissionId FROM Author a " +
           "JOIN Submission s ON a.publicationId = s.currentPublicationId " +
           "WHERE a.email = :email")
    List<Long> findSubmissionIdsByEmail(@Param("email") String email);
    
    /**
     * Count total submissions by author email
     */
    @Query("SELECT COUNT(DISTINCT s.submissionId) FROM Author a " +
           "JOIN Submission s ON a.publicationId = s.currentPublicationId " +
           "WHERE a.email = :email")
    Long countTotalSubmissionsByEmail(@Param("email") String email);
    
    /**
     * Count published articles (status = 3) by author email
     */
    @Query("SELECT COUNT(DISTINCT s.submissionId) FROM Author a " +
           "JOIN Submission s ON a.publicationId = s.currentPublicationId " +
           "WHERE a.email = :email AND s.status = 3")
    Long countPublishedByEmail(@Param("email") String email);
    
    /**
     * Count articles under review (status = 1) by author email
     */
    @Query("SELECT COUNT(DISTINCT s.submissionId) FROM Author a " +
           "JOIN Submission s ON a.publicationId = s.currentPublicationId " +
           "WHERE a.email = :email AND s.status = 1")
    Long countUnderReviewByEmail(@Param("email") String email);
    
    /**
     * Count declined articles (status = 4) by author email
     */
    @Query("SELECT COUNT(DISTINCT s.submissionId) FROM Author a " +
           "JOIN Submission s ON a.publicationId = s.currentPublicationId " +
           "WHERE a.email = :email AND s.status = 4")
    Long countDeclinedByEmail(@Param("email") String email);
    
    // ============= Review Time Queries =============
    
    /**
     * Calculate average review days for published articles
     */
    @Query(value = "SELECT AVG(DATEDIFF(p.date_published, s.date_submitted)) " +
                   "FROM authors a " +
                   "JOIN submissions s ON a.publication_id = s.current_publication_id " +
                   "JOIN publications p ON s.current_publication_id = p.publication_id " +
                   "WHERE a.email = :email AND s.status = 3 AND p.date_published IS NOT NULL " +
                   "AND s.date_submitted IS NOT NULL",
           nativeQuery = true)
    Double calculateAverageReviewDays(@Param("email") String email);
    
    /**
     * Find fastest review time (minimum days)
     */
    @Query(value = "SELECT MIN(DATEDIFF(p.date_published, s.date_submitted)) " +
                   "FROM authors a " +
                   "JOIN submissions s ON a.publication_id = s.current_publication_id " +
                   "JOIN publications p ON s.current_publication_id = p.publication_id " +
                   "WHERE a.email = :email AND s.status = 3 AND p.date_published IS NOT NULL " +
                   "AND s.date_submitted IS NOT NULL",
           nativeQuery = true)
    Long findFastestReviewDays(@Param("email") String email);
    
    /**
     * Find longest review time (maximum days)
     */
    @Query(value = "SELECT MAX(DATEDIFF(p.date_published, s.date_submitted)) " +
                   "FROM authors a " +
                   "JOIN submissions s ON a.publication_id = s.current_publication_id " +
                   "JOIN publications p ON s.current_publication_id = p.publication_id " +
                   "WHERE a.email = :email AND s.status = 3 AND p.date_published IS NOT NULL " +
                   "AND s.date_submitted IS NOT NULL",
           nativeQuery = true)
    Long findLongestReviewDays(@Param("email") String email);
    
    /**
     * Find submissions currently in review with days pending
     * Returns: submission_id, days_pending
     */
    @Query(value = "SELECT s.submission_id, DATEDIFF(CURDATE(), s.date_submitted) as days_pending " +
                   "FROM authors a " +
                   "JOIN submissions s ON a.publication_id = s.current_publication_id " +
                   "WHERE a.email = :email AND s.status = 1 AND s.date_submitted IS NOT NULL " +
                   "ORDER BY days_pending DESC",
           nativeQuery = true)
    List<Object[]> findCurrentInReview(@Param("email") String email);
    
    // ============= Productivity Queries =============
    
    /**
     * Count articles published this year
     */
    @Query(value = "SELECT COUNT(DISTINCT s.submission_id) " +
                   "FROM authors a " +
                   "JOIN submissions s ON a.publication_id = s.current_publication_id " +
                   "JOIN publications p ON s.current_publication_id = p.publication_id " +
                   "WHERE a.email = :email AND s.status = 3 " +
                   "AND YEAR(p.date_published) = YEAR(CURDATE())",
           nativeQuery = true)
    Long countArticlesThisYear(@Param("email") String email);
    
    /**
     * Count articles published this month
     */
    @Query(value = "SELECT COUNT(DISTINCT s.submission_id) " +
                   "FROM authors a " +
                   "JOIN submissions s ON a.publication_id = s.current_publication_id " +
                   "JOIN publications p ON s.current_publication_id = p.publication_id " +
                   "WHERE a.email = :email AND s.status = 3 " +
                   "AND YEAR(p.date_published) = YEAR(CURDATE()) " +
                   "AND MONTH(p.date_published) = MONTH(CURDATE())",
           nativeQuery = true)
    Long countArticlesThisMonth(@Param("email") String email);
    
    /**
     * Get yearly breakdown of publications
     * Returns: year, count
     */
    @Query(value = "SELECT YEAR(p.date_published) as year, COUNT(DISTINCT s.submission_id) as count " +
                   "FROM authors a " +
                   "JOIN submissions s ON a.publication_id = s.current_publication_id " +
                   "JOIN publications p ON s.current_publication_id = p.publication_id " +
                   "WHERE a.email = :email AND s.status = 3 AND p.date_published IS NOT NULL " +
                   "GROUP BY YEAR(p.date_published) " +
                   "ORDER BY year DESC",
           nativeQuery = true)
    List<Object[]> findYearlyBreakdown(@Param("email") String email);
    
    // ============= Ranking Queries =============
    
    /**
     * Calculate all authors' scores for ranking
     * Returns: email, total_submissions, published_count, acceptance_rate, recent_count, score
     */
    @Query(value = "SELECT " +
                   "a.email, " +
                   "COUNT(DISTINCT s.submission_id) as total_submissions, " +
                   "SUM(CASE WHEN s.status = 3 THEN 1 ELSE 0 END) as published_count, " +
                   "CASE WHEN COUNT(DISTINCT s.submission_id) > 0 " +
                   "THEN (SUM(CASE WHEN s.status = 3 THEN 1 ELSE 0 END) * 100.0 / COUNT(DISTINCT s.submission_id)) " +
                   "ELSE 0 END as acceptance_rate, " +
                   "SUM(CASE WHEN p.date_published >= DATE_SUB(CURDATE(), INTERVAL 1 YEAR) AND s.status = 3 " +
                   "THEN 1 ELSE 0 END) as recent_count " +
                   "FROM authors a " +
                   "JOIN submissions s ON a.publication_id = s.current_publication_id " +
                   "LEFT JOIN publications p ON s.current_publication_id = p.publication_id " +
                   "GROUP BY a.email",
           nativeQuery = true)
    List<Object[]> calculateAllAuthorsScores();
    
    /**
     * Count total distinct authors
     */
    @Query("SELECT COUNT(DISTINCT a.email) FROM Author a WHERE a.email IS NOT NULL")
    Long countTotalAuthors();
    
    // ============= Timeline Queries =============
    
    /**
     * Get all submissions with timeline details for an author
     * Returns: submission_id, status, stage_id, date_submitted, date_published
     */
    @Query(value = "SELECT s.submission_id, s.status, s.stage_id, s.date_submitted, p.date_published " +
                   "FROM authors a " +
                   "JOIN submissions s ON a.publication_id = s.current_publication_id " +
                   "LEFT JOIN publications p ON s.current_publication_id = p.publication_id " +
                   "WHERE a.email = :email " +
                   "ORDER BY s.date_submitted DESC",
           nativeQuery = true)
    List<Object[]> findSubmissionTimeline(@Param("email") String email);
    
    /**
     * Calculate average days in each stage across all submissions
     * Returns: stage_id, avg_days
     */
    @Query(value = "SELECT s.stage_id, AVG(DATEDIFF(COALESCE(p.date_published, CURDATE()), s.date_submitted)) as avg_days " +
                   "FROM submissions s " +
                   "LEFT JOIN publications p ON s.current_publication_id = p.publication_id " +
                   "WHERE s.date_submitted IS NOT NULL " +
                   "GROUP BY s.stage_id",
           nativeQuery = true)
    List<Object[]> findAverageDaysPerStage();
}
