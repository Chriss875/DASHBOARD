package org.udsm.udsm_hackathon2026.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyMetricsDto {
    private String month;      // Format: "202005" or "May 2020"
    private Integer year;      // Year: 2020
    private Integer monthNum;  // Month number: 5 (for May)
    private Long views;        // Total views for that month
    private Long downloads;
}
