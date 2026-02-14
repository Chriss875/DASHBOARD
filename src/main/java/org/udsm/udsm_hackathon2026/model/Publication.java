package org.udsm.udsm_hackathon2026.model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "publications")
@Getter
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
}
