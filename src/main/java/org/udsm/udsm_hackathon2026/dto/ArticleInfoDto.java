package org.udsm.udsm_hackathon2026.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleInfoDto {
    private Long articleId;
    private String title;
    private String articleAbstract;
    private List<String> authors;
}
