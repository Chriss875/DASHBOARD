package org.udsm.udsm_hackathon2026.dto.realtime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Galley (file) information for download events")
public class GalleyDto {
    
    @Schema(description = "Galley ID", example = "87")
    private Long galleyId;
    
    @Schema(description = "Galley label (e.g., PDF, HTML)", example = "PDF")
    private String galleyLabel;
    
    @Schema(description = "File ID", example = "234")
    private Long fileId;
    
    @Schema(description = "MIME type of the file", example = "application/pdf")
    private String mimeType;
    
    @Schema(description = "File name", example = "1542-article.pdf")
    private String fileName;
}
