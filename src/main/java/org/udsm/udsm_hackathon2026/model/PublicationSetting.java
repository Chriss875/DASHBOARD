package org.udsm.udsm_hackathon2026.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "publication_settings")
@IdClass(PublicationSettingId.class)
@Getter
@NoArgsConstructor
public class PublicationSetting {

    @Id
    @Column(name = "publication_id")
    private Long publicationId;

    @Id
    @Column(name = "locale")
    private String locale;

    @Id
    @Column(name = "setting_name")
    private String settingName;

    @Column(name = "setting_value", columnDefinition = "MEDIUMTEXT")
    private String settingValue;

    @ManyToOne
    @JoinColumn(name = "publication_id", referencedColumnName = "publication_id", insertable = false, updatable = false)
    private Publication publication;
}
