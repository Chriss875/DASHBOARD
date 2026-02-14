package org.udsm.udsm_hackathon2026.model;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "metrics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Metric {

    /**
     * The metrics table uses load_id as unique identifier for each metric entry.
     * For real-time ingestion, we generate a UUID for each new event.
     */
    @Id
    @Column(name = "load_id")
    private String loadId;

    @Column(name = "context_id")
    private Long contextId;

    @Column(name = "submission_id")
    private Long submissionId;

    @Column(name = "assoc_id")
    private Long assocId;

    @Column(name = "assoc_type")
    private Long assocType; // 1048585 = reads, 515 = downloads

    @Column(name = "representation_id")
    private Long representationId;

    @Column(name = "day")
    private String day; // Format: YYYYMMDD

    @Column(name = "month")
    private String month; // Format: YYYYMM

    @Column(name = "file_type")
    private Short fileType;

    @Column(name = "country_id", length = 2)
    private String countryId; // ISO 2-letter code from GeoIP

    @Column(name = "region", length = 2)
    private String region; // Region code from GeoIP

    @Column(name = "city")
    private String city; // City name from GeoIP

    @Column(name = "metric_type")
    private String metricType; // e.g., "ojs::counter"

    @Column(name = "metric")
    private Integer metric; // Count (typically 1 per event)
}