package org.udsm.udsm_hackathon2026.model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "citations")
@Getter
@NoArgsConstructor
public class Citation {

    @Id
    @Column(name = "citation_id")
    private Long citationId;

    @Column(name = "publication_id")
    private Long publicationId;

    @Column(name = "raw_citation", columnDefinition = "TEXT")
    private String rawCitation;

    @Column(name = "seq")
    private Long seq;
}