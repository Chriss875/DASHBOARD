package org.udsm.udsm_hackathon2026.dto.realtime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Aggregated statistics for WebSocket /topic/stats broadcasting")
public class StatsDto {
    
    @Schema(description = "Total reads across all articles", example = "14523")
    private Long totalReads;
    
    @Schema(description = "Total downloads across all articles", example = "3201")
    private Long totalDownloads;
    
    @Schema(description = "Reads in the last 5 minutes", example = "42")
    private Long readsLast5Min;
    
    @Schema(description = "Downloads in the last 5 minutes", example = "11")
    private Long downloadsLast5Min;
    
    @Schema(description = "Number of unique countries", example = "27")
    private Long uniqueCountries;
    
    @Schema(description = "Number of unique IP addresses", example = "189")
    private Long uniqueIPs;
}
