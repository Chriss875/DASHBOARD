package org.udsm.udsm_hackathon2026.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.udsm.udsm_hackathon2026.model.Publication;


@Repository
public interface PublicationRepository extends JpaRepository<Publication, Long> {
}