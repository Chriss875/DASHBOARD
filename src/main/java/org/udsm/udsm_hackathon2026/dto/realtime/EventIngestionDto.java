package org.udsm.udsm_hackathon2026.dto.realtime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Event ingestion payload from OJS plugin")
public class EventIngestionDto {
    
    @Schema(description = "Event type: READ or DOWNLOAD", example = "READ", allowableValues = {"READ", "DOWNLOAD"})
    private String eventType;
    
    @Schema(description = "Timestamp of the event in ISO-8601 format", example = "2026-02-14T12:53:00Z")
    private String timestamp;
    
    @Schema(description = "IP address of the user", example = "197.232.45.12")
    private String ip;
    
    @Schema(description = "User agent string", example = "Mozilla/5.0 ...")
    private String userAgent;
    
    @Schema(description = "HTTP referrer", example = "https://google.com/...")
    private String referrer;
    
    @Schema(description = "Journal path/identifier", example = "tjpsd")
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
    
    @Schema(description = "List of authors", example = "[\"John Doe\", \"Jane Smith\"]")
    private List<String> authors;
    
    @Schema(description = "Galley information (only for DOWNLOAD events)")
    private GalleyDto galley;
}
