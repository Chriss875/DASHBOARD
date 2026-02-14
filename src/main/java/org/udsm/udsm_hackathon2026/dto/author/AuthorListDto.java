package org.udsm.udsm_hackathon2026.dto.author;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for returning author information in list endpoints
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Author information including contact details")
public class AuthorListDto {
    
    @Schema(description = "Unique author identifier", example = "12345")
    private Long authorId;
    
    @Schema(description = "Author's given name (first name)", example = "John")
    private String givenName;
    
    @Schema(description = "Author's family name (last name)", example = "Doe")
    private String familyName;
    
    @Schema(description = "Author's full name", example = "John Doe")
    private String fullName;
    
    @Schema(description = "Author's email address", example = "john.doe@udsm.ac.tz")
    private String email;
    
    @Schema(description = "Author's phone number", example = "+255123456789")
    private String phone;
    
    @Schema(description = "Author's affiliation/institution", example = "University of Dar es Salaam")
    private String affiliation;
    
    @Schema(description = "Author's country", example = "Tanzania")
    private String country;
}
