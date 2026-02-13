package org.udsm.udsm_hackathon2026.model;
import lombok.Data;
import jakarta.persistence.*;


@Entity
@Table(name = "submissions")
@Data
public class Submission {

    @Id
    @Column(name = "submission_id")
    private Long submissionId;

    @Column(name = "context_id")
    private Long contextId;

    @Column(name = "current_publication_id")
    private Long currentPublicationId;

    @Column(name = "status")
    private Integer status;

    // Relationship with Publication
    @OneToOne
    @JoinColumn(name = "current_publication_id", referencedColumnName = "publication_id", insertable = false, updatable = false)
    private Publication publication;
}