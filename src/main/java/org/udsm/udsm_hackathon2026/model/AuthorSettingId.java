package org.udsm.udsm_hackathon2026.model;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class AuthorSettingId implements Serializable {
    private Long authorId;
    private String locale;
    private String settingName;
}
