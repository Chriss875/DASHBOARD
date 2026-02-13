package org.udsm.udsm_hackathon2026.model;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PublicationSettingId implements Serializable {
    private Long publicationId;
    private String locale;
    private String settingName;
}
