package org.udsm.udsm_hackathon2026.model;
import com.fasterxml.jackson.annotation.JsonIgnore;  // ← ADD THIS
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "publication_citation_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicationCitationHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @Column(name = "publication_id", nullable = false)
    private Long publicationId;

    @Column(name = "citation_count", nullable = false)
    private Integer citationCount;

    @Builder.Default  // ← ADD THIS
    @Column(name = "previous_count")
    private Integer previousCount = 0;

    @Column(name = "checked_at", nullable = false)
    private LocalDateTime checkedAt;

    @Builder.Default  // ← ADD THIS
    @Column(name = "source", length = 50)
    private String source = "crossref";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publication_id", insertable = false, updatable = false)
    @JsonIgnore  // ← ADD THIS - Prevents infinite loop!
    private Publication publication;
}