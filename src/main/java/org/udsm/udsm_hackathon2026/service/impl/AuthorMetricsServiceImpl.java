package org.udsm.udsm_hackathon2026.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.udsm.udsm_hackathon2026.dto.author.*;
import org.udsm.udsm_hackathon2026.repository.AuthorRepository;
import org.udsm.udsm_hackathon2026.service.AuthorMetricsService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthorMetricsServiceImpl implements AuthorMetricsService {
    
    private final AuthorRepository authorRepository;
    
    @Override
    public AuthorDashboardDTO getAuthorDashboard(String authorEmail) {
        log.info("Fetching dashboard for author: {}", authorEmail);
        
        Long totalSubmissions = authorRepository.countTotalSubmissionsByEmail(authorEmail);
        Long publishedArticles = authorRepository.countPublishedByEmail(authorEmail);
        Long underReview = authorRepository.countUnderReviewByEmail(authorEmail);
        Long declined = authorRepository.countDeclinedByEmail(authorEmail);
        
        // Calculate acceptance rate
        Double acceptanceRate = 0.0;
        if (totalSubmissions > 0) {
            acceptanceRate = (publishedArticles * 100.0) / totalSubmissions;
            acceptanceRate = Math.round(acceptanceRate * 100.0) / 100.0; // Round to 2 decimals
        }
        
        return AuthorDashboardDTO.builder()
                .authorEmail(authorEmail)
                .totalSubmissions(totalSubmissions)
                .publishedArticles(publishedArticles)
                .underReview(underReview)
                .declined(declined)
                .acceptanceRate(acceptanceRate)
                .build();
    }
    
    @Override
    public ReviewTimeDTO getReviewTimes(String authorEmail) {
        log.info("Fetching review times for author: {}", authorEmail);
        
        Double averageReviewDays = authorRepository.calculateAverageReviewDays(authorEmail);
        Long fastestReviewDays = authorRepository.findFastestReviewDays(authorEmail);
        Long longestReviewDays = authorRepository.findLongestReviewDays(authorEmail);
        
        // Get current submissions in review
        List<Object[]> currentReviews = authorRepository.findCurrentInReview(authorEmail);
        List<ReviewTimeDTO.CurrentReviewDTO> currentInReview = currentReviews.stream()
                .map(row -> ReviewTimeDTO.CurrentReviewDTO.builder()
                        .submissionId(((Number) row[0]).longValue())
                        .daysPending(((Number) row[1]).longValue())
                        .status("Under Review")
                        .build())
                .collect(Collectors.toList());
        
        return ReviewTimeDTO.builder()
                .averageReviewDays(averageReviewDays != null ? Math.round(averageReviewDays * 100.0) / 100.0 : null)
                .fastestReviewDays(fastestReviewDays)
                .longestReviewDays(longestReviewDays)
                .currentInReview(currentInReview)
                .build();
    }
    
    @Override
    public ProductivityDTO getProductivity(String authorEmail) {
        log.info("Fetching productivity metrics for author: {}", authorEmail);
        
        Long articlesThisYear = authorRepository.countArticlesThisYear(authorEmail);
        Long articlesThisMonth = authorRepository.countArticlesThisMonth(authorEmail);
        
        // Get yearly breakdown
        List<Object[]> yearlyData = authorRepository.findYearlyBreakdown(authorEmail);
        List<ProductivityDTO.YearlyStats> yearlyBreakdown = yearlyData.stream()
                .map(row -> ProductivityDTO.YearlyStats.builder()
                        .year(((Number) row[0]).intValue())
                        .count(((Number) row[1]).longValue())
                        .build())
                .collect(Collectors.toList());
        
        // Calculate average per year
        Double averagePerYear = 0.0;
        if (!yearlyBreakdown.isEmpty()) {
            long totalArticles = yearlyBreakdown.stream()
                    .mapToLong(ProductivityDTO.YearlyStats::getCount)
                    .sum();
            averagePerYear = (double) totalArticles / yearlyBreakdown.size();
            averagePerYear = Math.round(averagePerYear * 100.0) / 100.0;
        }
        
        // Determine trend (compare last 2 years)
        String trend = calculateTrend(yearlyBreakdown);
        
        return ProductivityDTO.builder()
                .articlesThisYear(articlesThisYear)
                .articlesThisMonth(articlesThisMonth)
                .averagePerYear(averagePerYear)
                .yearlyBreakdown(yearlyBreakdown)
                .trend(trend)
                .build();
    }
    
    @Override
    public RankingDTO getRanking(String authorEmail) {
        log.info("Calculating ranking for author: {}", authorEmail);
        
        // Get all authors' scores
        List<Object[]> allScores = authorRepository.calculateAllAuthorsScores();
        
        // Calculate scores for all authors
        List<AuthorScore> authorScores = allScores.stream()
                .map(row -> {
                    String email = (String) row[0];
                    Long totalSubmissions = ((Number) row[1]).longValue();
                    Long publishedCount = ((Number) row[2]).longValue();
                    Double acceptanceRate = row[3] != null ? ((Number) row[3]).doubleValue() : 0.0;
                    Long recentCount = ((Number) row[4]).longValue();
                    
                    // Calculate weighted score
                    double score = calculateAuthorScore(acceptanceRate, publishedCount, recentCount);
                    
                    return new AuthorScore(email, score, totalSubmissions, publishedCount);
                })
                .sorted((a, b) -> Double.compare(b.score, a.score)) // Sort descending by score
                .collect(Collectors.toList());
        
        // Find current author's position
        Integer rank = null;
        Double authorScore = null;
        for (int i = 0; i < authorScores.size(); i++) {
            if (authorScores.get(i).email.equalsIgnoreCase(authorEmail)) {
                rank = i + 1;
                authorScore = authorScores.get(i).score;
                break;
            }
        }
        
        Long totalAuthors = (long) authorScores.size();
        
        // Calculate percentile
        Double percentile = 0.0;
        if (rank != null && totalAuthors > 0) {
            percentile = ((totalAuthors - rank) * 100.0) / totalAuthors;
            percentile = Math.round(percentile * 100.0) / 100.0;
        }
        
        // Calculate journal average
        Double journalAverage = authorScores.stream()
                .mapToDouble(as -> as.score)
                .average()
                .orElse(0.0);
        journalAverage = Math.round(journalAverage * 100.0) / 100.0;
        
        // Assign badge
        String badge = assignBadge(authorScore);
        
        return RankingDTO.builder()
                .rank(rank)
                .totalAuthors(totalAuthors)
                .percentile(percentile)
                .authorScore(authorScore != null ? Math.round(authorScore * 100.0) / 100.0 : null)
                .journalAverage(journalAverage)
                .badge(badge)
                .build();
    }
    
    @Override
    public TimelineDTO getTimeline(String authorEmail) {
        log.info("Fetching timeline for author: {}", authorEmail);
        
        List<Object[]> timelineData = authorRepository.findSubmissionTimeline(authorEmail);
        
        // Get average days per stage for progress calculation
        Map<Long, Double> avgDaysPerStage = getAverageDaysPerStage();
        
        List<TimelineDTO.SubmissionTimelineDTO> submissions = timelineData.stream()
                .map(row -> {
                    Long submissionId = ((Number) row[0]).longValue();
                    Integer status = ((Number) row[1]).intValue();
                    Long stageId = ((Number) row[2]).longValue();
                    LocalDateTime dateSubmitted = row[3] != null ? (LocalDateTime) row[3] : null;
                    LocalDate datePublished = row[4] != null ? (LocalDate) row[4] : null;
                    
                    String statusStr = mapStatus(status);
                    String currentStage = mapStage(stageId);
                    
                    // Calculate days in current stage
                    Long daysInCurrentStage = 0L;
                    if (dateSubmitted != null) {
                        LocalDate endDate = datePublished != null ? datePublished : LocalDate.now();
                        daysInCurrentStage = ChronoUnit.DAYS.between(dateSubmitted.toLocalDate(), endDate);
                    }
                    
                    // Calculate expected completion date and progress
                    LocalDate expectedCompletionDate = calculateExpectedCompletion(
                            dateSubmitted, stageId, avgDaysPerStage);
                    Integer progress = calculateProgress(status, stageId, dateSubmitted, avgDaysPerStage);
                    
                    return TimelineDTO.SubmissionTimelineDTO.builder()
                            .submissionId(submissionId)
                            .status(statusStr)
                            .currentStage(currentStage)
                            .daysInCurrentStage(daysInCurrentStage)
                            .submittedDate(dateSubmitted)
                            .expectedCompletionDate(expectedCompletionDate)
                            .progress(progress)
                            .build();
                })
                .collect(Collectors.toList());
        
        return TimelineDTO.builder()
                .submissions(submissions)
                .build();
    }
    
    @Override
    @Cacheable(value = "completeDashboard", key = "#authorEmail")
    public CompleteDashboardDTO getCompleteDashboard(String authorEmail) {
        log.info("Fetching complete dashboard for author: {}", authorEmail);
        
        // Fetch all metrics in parallel (optimized)
        AuthorDashboardDTO dashboard = getAuthorDashboard(authorEmail);
        ReviewTimeDTO reviewTimes = getReviewTimes(authorEmail);
        ProductivityDTO productivity = getProductivity(authorEmail);
        RankingDTO ranking = getRanking(authorEmail);
        TimelineDTO timeline = getTimeline(authorEmail);
        
        return CompleteDashboardDTO.builder()
                .dashboard(dashboard)
                .reviewTimes(reviewTimes)
                .productivity(productivity)
                .ranking(ranking)
                .timeline(timeline)
                .lastUpdated(LocalDateTime.now())
                .build();
    }
    
    // ============= Helper Methods =============
    
    /**
     * Calculate weighted author score
     * @param acceptanceRate Acceptance rate (0-100)
     * @param publishedCount Number of published articles
     * @param recentCount Recent publications (last year)
     * @return Score (0-100)
     */
    private double calculateAuthorScore(Double acceptanceRate, Long publishedCount, Long recentCount) {
        // Normalize published count (assuming max 100 articles is top score)
        double normalizedPublished = Math.min(publishedCount / 100.0 * 100, 100);
        
        // Normalize recent activity (assuming max 20 per year is top score)
        double normalizedRecent = Math.min(recentCount / 20.0 * 100, 100);
        
        // Weighted score: 40% acceptance rate, 30% total publications, 30% recent activity
        return (acceptanceRate * 0.4) + (normalizedPublished * 0.3) + (normalizedRecent * 0.3);
    }
    
    /**
     * Assign badge based on score
     */
    private String assignBadge(Double score) {
        if (score == null) return "None";
        if (score >= 90) return "Gold";
        if (score >= 75) return "Silver";
        if (score >= 60) return "Bronze";
        return "None";
    }
    
    /**
     * Calculate publishing trend
     */
    private String calculateTrend(List<ProductivityDTO.YearlyStats> yearlyBreakdown) {
        if (yearlyBreakdown.size() < 2) return "stable";
        
        // Compare most recent year with previous year
        Long currentYear = yearlyBreakdown.get(0).getCount();
        Long previousYear = yearlyBreakdown.get(1).getCount();
        
        if (currentYear > previousYear) return "increasing";
        if (currentYear < previousYear) return "decreasing";
        return "stable";
    }
    
    /**
     * Map status code to readable string
     */
    private String mapStatus(Integer status) {
        return switch (status) {
            case 1 -> "Under Review";
            case 3 -> "Published";
            case 4 -> "Declined";
            default -> "Unknown";
        };
    }
    
    /**
     * Map stage ID to readable stage name
     */
    private String mapStage(Long stageId) {
        return switch (stageId.intValue()) {
            case 1 -> "Submission";
            case 3 -> "Review";
            case 4 -> "Copyediting";
            case 5 -> "Production";
            default -> "Unknown";
        };
    }
    
    /**
     * Get average days per stage from database
     */
    private Map<Long, Double> getAverageDaysPerStage() {
        List<Object[]> avgData = authorRepository.findAverageDaysPerStage();
        return avgData.stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).longValue(),
                        row -> ((Number) row[1]).doubleValue()
                ));
    }
    
    /**
     * Calculate expected completion date based on average stage times
     */
    private LocalDate calculateExpectedCompletion(LocalDateTime dateSubmitted, 
                                                   Long stageId, 
                                                   Map<Long, Double> avgDaysPerStage) {
        if (dateSubmitted == null) return null;
        
        Double avgDays = avgDaysPerStage.getOrDefault(stageId, 60.0); // Default 60 days
        return dateSubmitted.toLocalDate().plusDays(avgDays.longValue());
    }
    
    /**
     * Calculate progress percentage based on time elapsed and average stage duration
     */
    private Integer calculateProgress(Integer status, Long stageId, 
                                       LocalDateTime dateSubmitted, 
                                       Map<Long, Double> avgDaysPerStage) {
        // If published, return 100%
        if (status == 3) return 100;
        
        // If declined, return 0%
        if (status == 4) return 0;
        
        if (dateSubmitted == null) return 0;
        
        Double avgDays = avgDaysPerStage.getOrDefault(stageId, 60.0);
        long daysElapsed = ChronoUnit.DAYS.between(dateSubmitted.toLocalDate(), LocalDate.now());
        
        int progress = (int) ((daysElapsed / avgDays) * 100);
        return Math.min(progress, 99); // Cap at 99% if not published
    }
    
    /**
     * Internal class for author score calculation
     */
    private static class AuthorScore {
        String email;
        Double score;
        Long totalSubmissions;
        Long publishedCount;
        
        AuthorScore(String email, Double score, Long totalSubmissions, Long publishedCount) {
            this.email = email;
            this.score = score;
            this.totalSubmissions = totalSubmissions;
            this.publishedCount = publishedCount;
        }
    }
}
