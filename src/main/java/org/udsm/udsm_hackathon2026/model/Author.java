package org.udsm.udsm_hackathon2026.model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "authors")
@Getter
@NoArgsConstructor
public class Author {

    @Id
    @Column(name = "author_id")
    private Long authorId;

    @Column(name = "publication_id")
    private Long publicationId;

    @Column(name = "email",nullable = false)
    private String email;

    @Column(name = "seq")
    private Double seq;
}
