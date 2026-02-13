package org.udsm.udsm_hackathon2026.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.udsm.udsm_hackathon2026.model.Author;
import java.util.List;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {
    List<Author> findByPublicationIdOrderBySeqAsc(Long publicationId);
}
