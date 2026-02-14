package org.udsm.udsm_hackathon2026.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "metrics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiveEvent {
    
    @Id
    @Column(name = "load_id")
    private String loadId;
    
    @Column(name = "context_id", nullable = false)
    private Long contextId;
    
    @Column(name = "pkp_section_id")
    private Long pkpSectionId;
    
    @Column(name = "assoc_object_type")
    private Long assocObjectType;
    
    @Column(name = "assoc_object_id")
    private Long assocObjectId;
    
    @Column(name = "submission_id")
    private Long submissionId;
    
    @Column(name = "representation_id")
    private Long representationId;
    
    @Column(name = "assoc_type", nullable = false)
    private Long assocType; // 1048585 = READ, 515 = DOWNLOAD
    
    @Column(name = "assoc_id", nullable = false)
    private Long assocId;
    
    @Column(name = "day", length = 8)
    private String day; // Format: "20260214"
    
    @Column(name = "month", length = 6)
    private String month; // Format: "202602"
    
    @Column(name = "file_type")
    private Integer fileType;
    
    @Column(name = "country_id", length = 2)
    private String countryId;
    
    @Column(name = "region", length = 2)
    private String region;
    
    @Column(name = "city")
    private String city;
    
    @Column(name = "metric_type", nullable = false)
    private String metricType;
    
    @Column(name = "metric", nullable = false)
    private Integer metric;
    
    // Additional fields for enriched events (these will be stored in other ways or derived)
    @Transient
    private String ip;
    
    @Transient
    private String userAgent;
    
    @Transient
    private String referrer;
    
    @Transient
    private String journalPath;
    
    @Transient
    private String journalTitle;
    
    @Transient
    private String articleTitle;
    
    @Transient
    private String doi;
    
    @Transient
    private String sectionTitle;
    
    @Transient
    private String authorsJson;
    
    @Transient
    private String galleyLabel;
    
    @Transient
    private String galleyMimeType;
    
    @Transient
    private String galleyFileName;
    
    @Transient
    private String country;
    
    @Transient
    private String countryCode;
    
    @Transient
    private String continent;
    
    @Transient
    private Double latitude;
    
    @Transient
    private Double longitude;
    
    @Transient
    private LocalDateTime timestamp;
}
