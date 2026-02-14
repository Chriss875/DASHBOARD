package org.udsm.udsm_hackathon2026.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Monthly metrics data transfer object for time-series analytics")
public class MonthlyMetricsDto {
    @Schema(description = "Month in string format", example = "May 2020")
    private String month;      // Format: "202005" or "May 2020"
    
    @Schema(description = "Year", example = "2020")
    private Integer year;      // Year: 2020
    
    @Schema(description = "Month number (1-12)", example = "5")
    private Integer monthNum;  // Month number: 5 (for May)
    
    @Schema(description = "Total views for that month", example = "1234")
    private Long views;        // Total views for that month
    
    @Schema(description = "Total downloads for that month", example = "567")
    private Long downloads;
}
