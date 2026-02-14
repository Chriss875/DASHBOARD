package org.udsm.udsm_hackathon2026.service;
import org.udsm.udsm_hackathon2026.dto.ArticleStatusDto;
import org.udsm.udsm_hackathon2026.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ArticleStatisticsServiceImpl implements ArticleStatisticsService {
    private final SubmissionRepository submissionRepository;
    private static final Integer STATUS_UNDER_REVIEW = 1;
    private static final Integer STATUS_PUBLISHED = 3;
    private static final Integer STATUS_DECLINED = 4;

    @Override
    public ArticleStatusDto getPublishedArticlesCount() {
        log.info("Fetching published articles count");

        Long publishedCount = submissionRepository.countPublishedArticles();
        Long totalSubmitted = submissionRepository.countTotalSubmittedArticles();

        log.info("Published articles: {}, Total submitted: {}", publishedCount, totalSubmitted);

        return ArticleStatusDto.forPublished(publishedCount, totalSubmitted);
    }

    @Override
    public ArticleStatusDto getDeclinedArticlesCount() {
        log.info("Fetching declined articles count");

        Long declinedCount = submissionRepository.countDeclinedArticles();
        Long totalSubmitted = submissionRepository.countTotalSubmittedArticles();

        log.info("Declined articles: {}, Total submitted: {}", declinedCount, totalSubmitted);

        return ArticleStatusDto.forDeclined(declinedCount, totalSubmitted);
    }

    @Override
    public ArticleStatusDto getUnderReviewArticlesCount() {
        log.info("Fetching articles under review count");

        Long underReviewCount = submissionRepository.countUnderReviewArticles();
        Long totalSubmitted = submissionRepository.countTotalSubmittedArticles();

        log.info("Articles under review: {}, Total submitted: {}", underReviewCount, totalSubmitted);

        return ArticleStatusDto.forUnderReview(underReviewCount, totalSubmitted);
    }

    @Override
    public ArticleStatusDto getTotalSubmittedArticlesCount() {
        log.info("Fetching total submitted articles count");

        Long totalSubmitted = submissionRepository.countTotalSubmittedArticles();

        log.info("Total submitted articles: {}", totalSubmitted);

        return ArticleStatusDto.forSubmitted(totalSubmitted);
    }
}