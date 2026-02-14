package org.udsm.udsm_hackathon2026.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Article status statistics data transfer object")
public class ArticleStatusDto {
    @Schema(description = "Total number of published articles (status = 3)", example = "179")
    private Long totalPublished;
    
    @Schema(description = "Total number of submitted articles (all statuses)", example = "230")
    private Long totalSubmitted;
    
    @Schema(description = "Total number of articles under review (status = 1)", example = "44")
    private Long totalUnderReview;
    
    @Schema(description = "Total number of declined articles (status = 4)", example = "7")
    private Long totalDeclined;

    // Constructor for individual status count
    public ArticleStatusDto(Long count) {
        this.totalPublished = 0L;
        this.totalSubmitted = 0L;
        this.totalUnderReview = 0L;
        this.totalDeclined = 0L;
    }

    // Static factory methods for creating specific status DTOs
    public static ArticleStatusDto forPublished(Long publishedCount, Long totalSubmitted) {
        ArticleStatusDto dto = new ArticleStatusDto();
        dto.setTotalPublished(publishedCount);
        dto.setTotalSubmitted(totalSubmitted);
        return dto;
    }

    public static ArticleStatusDto forDeclined(Long declinedCount, Long totalSubmitted) {
        ArticleStatusDto dto = new ArticleStatusDto();
        dto.setTotalDeclined(declinedCount);
        dto.setTotalSubmitted(totalSubmitted);
        return dto;
    }

    public static ArticleStatusDto forUnderReview(Long underReviewCount, Long totalSubmitted) {
        ArticleStatusDto dto = new ArticleStatusDto();
        dto.setTotalUnderReview(underReviewCount);
        dto.setTotalSubmitted(totalSubmitted);
        return dto;
    }

    public static ArticleStatusDto forSubmitted(Long totalSubmitted) {
        ArticleStatusDto dto = new ArticleStatusDto();
        dto.setTotalSubmitted(totalSubmitted);
        return dto;
    }
}
