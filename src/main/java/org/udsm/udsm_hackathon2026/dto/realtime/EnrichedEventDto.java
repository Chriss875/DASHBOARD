package org.udsm.udsm_hackathon2026.dto.realtime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Enriched event with geo data for WebSocket broadcasting and responses")
public class EnrichedEventDto {
    
    // Original event data
    @Schema(description = "Event type: READ or DOWNLOAD", example = "READ")
    private String eventType;
    
    @Schema(description = "Timestamp of the event", example = "2026-02-14T12:53:00Z")
    private Instant timestamp;
    
    @Schema(description = "IP address", example = "197.232.45.12")
    private String ip;
    
    @Schema(description = "User agent", example = "Mozilla/5.0 ...")
    private String userAgent;
    
    @Schema(description = "Journal path", example = "tjpsd")
    private String journalPath;
    
    @Schema(description = "Journal title", example = "Tanzania Journal of Population Studies and Development")
    private String journalTitle;
    
    @Schema(description = "Article ID", example = "1542")
    private Long articleId;
    
    @Schema(description = "Article title", example = "Impact of Climate Change on Agricultural Productivity")
    private String articleTitle;
    
    @Schema(description = "DOI", example = "10.1234/tjpsd.v1i2.1542")
    private String doi;
    
    @Schema(description = "Section title", example = "Research Articles")
    private String sectionTitle;
    
    @Schema(description = "Authors as JSON string", example = "[\"John Doe\", \"Jane Smith\"]")
    private String authorsJson;
    
    @Schema(description = "Galley label (for downloads)", example = "PDF")
    private String galleyLabel;
    
    @Schema(description = "Galley MIME type (for downloads)", example = "application/pdf")
    private String galleyMimeType;
    
    @Schema(description = "Galley file name (for downloads)", example = "1542-article.pdf")
    private String galleyFileName;
    
    // Geo-enriched data
    @Schema(description = "Country name", example = "Tanzania")
    private String country;
    
    @Schema(description = "Country code (ISO 2-letter)", example = "TZ")
    private String countryCode;
    
    @Schema(description = "City name", example = "Dar es Salaam")
    private String city;
    
    @Schema(description = "Continent name", example = "Africa")
    private String continent;
    
    @Schema(description = "Latitude", example = "-6.8")
    private Double latitude;
    
    @Schema(description = "Longitude", example = "39.28")
    private Double longitude;
}
