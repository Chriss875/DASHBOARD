package org.udsm.udsm_hackathon2026.model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
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

    // Relationship with PublicationSettings
    @OneToMany(mappedBy = "publication")
    private List<PublicationSetting> settings;
}
