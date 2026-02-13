package org.udsm.udsm_hackathon2026.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.udsm.udsm_hackathon2026.model.Citation;

@Repository
public interface CitationRepository extends JpaRepository<Citation, Long> {
    @Query("SELECT COUNT(c) FROM Citation c")
    long countTotalCitations();


    /**
     * Count citations for a specific article by publication ID
     */
    @Query(value = "SELECT COALESCE(COUNT(c.citation_id), 0) " +
            "FROM citations c " +
            "WHERE c.publication_id = :publicationId",
            nativeQuery = true)
    long countCitationsByPublicationId(@Param("publicationId") Long publicationId);
}