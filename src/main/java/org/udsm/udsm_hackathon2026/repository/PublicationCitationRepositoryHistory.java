package org.udsm.udsm_hackathon2026.repository;

import org.springframework.data.jpa.repository.JpaRepository;  // ADD THIS
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.udsm.udsm_hackathon2026.model.PublicationCitationHistory;
import java.util.List;

@Repository
public interface PublicationCitationRepositoryHistory
        extends JpaRepository<PublicationCitationHistory, Long> {  // ADD THIS
    //                      ↑ Entity class           ↑ ID type

    // Get citation history for a specific publication
    List<PublicationCitationHistory> findByPublicationIdOrderByCheckedAtDesc(Long publicationId);

    // Get recent citation changes (new citations detected)
    @Query("SELECT h FROM PublicationCitationHistory h WHERE h.citationCount > h.previousCount " +
            "ORDER BY h.checkedAt DESC")
    List<PublicationCitationHistory> findRecentCitationIncreases();

    // Get latest record for each publication
    @Query(value = "SELECT h1.* FROM publication_citation_history h1 " +
            "INNER JOIN (SELECT publication_id, MAX(checked_at) as max_date " +
            "FROM publication_citation_history GROUP BY publication_id) h2 " +
            "ON h1.publication_id = h2.publication_id AND h1.checked_at = h2.max_date",
            nativeQuery = true)
    List<PublicationCitationHistory> findLatestForAllPublications();
}