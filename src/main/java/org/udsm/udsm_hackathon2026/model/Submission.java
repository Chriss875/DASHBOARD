package org.udsm.udsm_hackathon2026.model;
import lombok.Data;
import jakarta.persistence.*;

import java.time.LocalDateTime;

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

    @OneToOne
    @JoinColumn(name = "current_publication_id", referencedColumnName = "publication_id", insertable = false, updatable = false)
    private Publication publication;


    @Column(name = "date_last_activity")
    private LocalDateTime dateLastActivity;

    @Column(name = "date_submitted")
    private LocalDateTime dateSubmitted;

    @Column(name = "last_modified")
    private LocalDateTime lastModified;

    @Column(name = "stage_id", nullable = false)
    private Long stageId = 1L;

    @Column(name = "status", nullable = false)
    private Integer status = 1;

    @Column(name = "submission_progress", nullable = false)
    private Integer submissionProgress = 1;

    @Column(name = "work_type")
    private Integer workType = 0;

    @Column(name = "locale", length = 14)
    private String locale;
}