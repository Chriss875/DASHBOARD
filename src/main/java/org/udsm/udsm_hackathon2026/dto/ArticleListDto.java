package org.udsm.udsm_hackathon2026.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Article listing data transfer object for dropdowns and filters")
public class ArticleListDto {
    @Schema(description = "Unique article identifier", example = "12345")
    private Long id;              // Article ID
    
    @Schema(description = "Article title/name", example = "Deep Learning Applications in Agriculture")
    private String name;          // Article title/name
}