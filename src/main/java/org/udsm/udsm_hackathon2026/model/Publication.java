package org.udsm.udsm_hackathon2026.model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Formula;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Entity
@Table(name = "publications")
@Getter
@Setter
@NoArgsConstructor
public class Publication {
    @Id
    @Column(name = "publication_id")
    private Long publicationId;

    @Column(name = "submission_id")
    private Long submissionId;

    @Column(name = "date_published")
    private LocalDate datePublished;

    @Column(name = "status")
    private Short status;

    @OneToMany(mappedBy = "publication")
    private List<PublicationSetting> settings;

    @Column(name = "access_status")
    private Long accessStatus;


    @Column(name = "last_modified")
    private LocalDateTime lastModified;

    @Column(name = "primary_contact_id")
    private Long primaryContactId;

    @Column(name = "section_id")
    private Long sectionId;

    @Column(name = "seq")
    private Double seq;

    @Column(name = "url_path", length = 64)
    private String urlPath;

    @Column(name = "version")
    private Long version;

    @Column(name = "`citation_count`") // Use backticks to force exact naming
    private Integer citationCount = 0;

    @Formula("(SELECT ps.setting_value FROM publication_settings ps " +
            "WHERE ps.publication_id = publication_id " +
            "AND ps.setting_name = 'pub-id::doi' LIMIT 1)")
    private String doi;

    @Column(name = "last_citation_check")
    private LocalDateTime lastCitationCheck;
}
