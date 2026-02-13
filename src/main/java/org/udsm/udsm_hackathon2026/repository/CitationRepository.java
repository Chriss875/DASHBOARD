package org.udsm.udsm_hackathon2026.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.udsm.udsm_hackathon2026.model.Citation;

@Repository
public interface CitationRepository extends JpaRepository<Citation, Long> {

    @Query("SELECT COUNT(c) FROM Citation c")
    long countTotalCitations();
}