package org.udsm.udsm_hackathon2026.model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "metrics")
@Getter
@NoArgsConstructor
public class Metric {

    /**
     * The metrics table has no single primary key in the schema.
     * We use a synthetic row id via @GeneratedValue — but since this
     * table is READ-ONLY from our side, Hibernate won't attempt inserts.
     * We map an arbitrary unique combo as the ID workaround.
     */
    @Id
    @Column(name = "assoc_id")
    private Long assocId;

    @Column(name = "load_id")
    private String loadId;

    @Column(name = "context_id")
    private Long contextId;

    @Column(name = "submission_id")
    private Long submissionId;

    @Column(name = "assoc_type")
    private Long assocType;

    @Column(name = "representation_id")
    private Long representationId;

    @Column(name = "day")
    private String day;

    @Column(name = "month")
    private String month;

    @Column(name = "file_type")
    private Short fileType;

    @Column(name = "country_id", length = 2)
    private String countryId;

    @Column(name = "region", length = 2)
    private String region;

    @Column(name = "city")
    private String city;

    @Column(name = "metric_type")
    private String metricType;

    @Column(name = "metric")
    private Integer metric;
}