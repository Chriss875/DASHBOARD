package org.udsm.udsm_hackathon2026.dto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CrossrefResponse {

    @JsonProperty("status")
    private String status;

    @JsonProperty("message-type")
    private String messageType;

    @JsonProperty("message-version")
    private String messageVersion;

    @JsonProperty("message")
    private CrossrefWork message;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CrossrefWork {
        // ⬇️ ALL THESE FIELDS SHOULD BE INSIDE CrossrefWork ⬇️

        @JsonProperty("DOI")
        private String doi;

        @JsonProperty("title")
        private String[] title;

        @JsonProperty("is-referenced-by-count")
        private Integer isReferencedByCount;

        @JsonProperty("URL")
        private String url;

        @JsonProperty("type")
        private String type;

        @JsonProperty("created")
        private DateParts created;

    } // ⬅️ CrossrefWork class ends here

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DateParts {
        @JsonProperty("date-parts")
        private int[][] dateParts;
    }
}