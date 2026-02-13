package org.udsm.udsm_hackathon2026.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.udsm.udsm_hackathon2026.model.AuthorSetting;
import org.udsm.udsm_hackathon2026.model.AuthorSettingId;
import java.util.List;


@Repository
public interface AuthorSettingRepository extends JpaRepository<AuthorSetting, AuthorSettingId> {
    List<AuthorSetting> findByAuthorIdInAndSettingNameIn(List<Long> authorIds, List<String> settingNames);
}
