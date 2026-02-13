package org.udsm.udsm_hackathon2026.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DownloadEventDto {
    private Long articleId;
    private String country;
}
