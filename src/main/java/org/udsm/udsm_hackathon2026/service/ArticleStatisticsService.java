package org.udsm.udsm_hackathon2026.service;

import org.udsm.udsm_hackathon2026.dto.ArticleStatusDto;

public interface ArticleStatisticsService {

    /**
     * Get total number of published articles with total submitted
     * @return ArticleStatusDTO containing published count and total submitted
     */
    ArticleStatusDto getPublishedArticlesCount();

    /**
     * Get total number of declined articles with total submitted
     * @return ArticleStatusDTO containing declined count and total submitted
     */
    ArticleStatusDto getDeclinedArticlesCount();

    /**
     * Get total number of articles under review with total submitted
     * @return ArticleStatusDTO containing under review count and total submitted
     */
    ArticleStatusDto getUnderReviewArticlesCount();

    /**
     * Get total number of submitted articles
     * @return ArticleStatusDTO containing total submitted count
     */
    ArticleStatusDto getTotalSubmittedArticlesCount();
}
