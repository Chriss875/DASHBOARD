package org.udsm.udsm_hackathon2026.dto;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CountryCountDto {
    private String country;
    private Long count;
}