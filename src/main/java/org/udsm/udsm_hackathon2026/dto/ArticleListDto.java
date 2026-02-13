package org.udsm.udsm_hackathon2026.dto;
import lombok.*;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleListDto {
    private Long id;              // Article ID
    private String name;          // Article title/name
}