package org.udsm.udsm_hackathon2026.service;

import org.udsm.udsm_hackathon2026.dto.author.*;

/**
 * Service interface for author metrics and dashboard functionality
 */
public interface AuthorMetricsService {
    
    /**
     * Get dashboard summary for an author
     * @param authorEmail Author's email address
     * @return Dashboard statistics including submissions, publications, and acceptance rate
     */
    AuthorDashboardDTO getAuthorDashboard(String authorEmail);
    
    /**
     * Get review time statistics for an author
     * @param authorEmail Author's email address
     * @return Review time metrics including average, fastest, and longest review times
     */
    ReviewTimeDTO getReviewTimes(String authorEmail);
    
    /**
     * Get productivity metrics for an author
     * @param authorEmail Author's email address
     * @return Productivity analysis including yearly breakdown and trends
     */
    ProductivityDTO getProductivity(String authorEmail);
    
    /**
     * Get ranking information for an author
     * @param authorEmail Author's email address
     * @return Ranking details including rank, percentile, score, and badge
     */
    RankingDTO getRanking(String authorEmail);
    
    /**
     * Get submission timeline for an author
     * @param authorEmail Author's email address
     * @return Timeline of all submissions with progress tracking
     */
    TimelineDTO getTimeline(String authorEmail);
    
    /**
     * Get complete dashboard combining all metrics
     * @param authorEmail Author's email address
     * @return Comprehensive dashboard with all metrics combined
     */
    CompleteDashboardDTO getCompleteDashboard(String authorEmail);
}
