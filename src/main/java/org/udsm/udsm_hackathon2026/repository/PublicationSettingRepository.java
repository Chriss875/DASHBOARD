package org.udsm.udsm_hackathon2026.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.udsm.udsm_hackathon2026.model.PublicationSetting;
import org.udsm.udsm_hackathon2026.model.PublicationSettingId;
import java.util.List;

@Repository
public interface PublicationSettingRepository extends JpaRepository<PublicationSetting, PublicationSettingId> {

    List<PublicationSetting> findByPublicationIdAndSettingNameIn(Long publicationId, List<String> settingNames);

    /**
     * Batch-fetch titles for a list of submission_ids.
     * In OJS, publication_settings uses publication_id which links to publications.publication_id.
     * publications.submission_id is what metrics uses.
     * So we need a join to go from submission_id → publication_id → title.
     */
    @Query("SELECT p.submissionId, ps.settingValue " +
            "FROM Publication p JOIN PublicationSetting ps ON p.publicationId = ps.publicationId " +
            "WHERE p.submissionId IN :submissionIds " +
            "AND ps.settingName = 'title'")
    List<Object[]> findTitlesBySubmissionIds(@Param("submissionIds") List<Long> submissionIds);
}