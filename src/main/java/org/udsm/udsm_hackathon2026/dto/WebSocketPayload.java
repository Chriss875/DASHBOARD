package org.udsm.udsm_hackathon2026.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketPayload {
    private Long articleId;
    private String type;            // "readership" or "download"
    private Object data;            // geo map: { "TZ": 11, "US": 5 }
    private ArticleInfoDto article; // title, authors, abstract
}