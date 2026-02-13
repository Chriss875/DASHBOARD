package org.udsm.udsm_hackathon2026.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "author_settings")
@IdClass(AuthorSettingId.class)
@Getter
@NoArgsConstructor
public class AuthorSetting {

    @Id
    @Column(name = "author_id")
    private Long authorId;

    @Id
    @Column(name = "locale")
    private String locale;

    @Id
    @Column(name = "setting_name")
    private String settingName;

    @Column(name = "setting_value", columnDefinition = "TEXT")
    private String settingValue;
}
