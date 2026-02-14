package org.udsm.udsm_hackathon2026.service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.udsm.udsm_hackathon2026.dto.author.AuthorListDto;
import org.udsm.udsm_hackathon2026.model.Author;
import org.udsm.udsm_hackathon2026.model.AuthorSetting;
import org.udsm.udsm_hackathon2026.repository.AuthorRepository;
import org.udsm.udsm_hackathon2026.repository.AuthorSettingRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for managing author list operations
 * Fetches author details including names, emails, and phone numbers from OJS database
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorListService {

    private final AuthorRepository authorRepository;
    private final AuthorSettingRepository authorSettingRepository;

    /**
     * Get all authors with their complete details
     * 
     * Fetches from:
     * - authors table: author_id, email
     * - author_settings table: givenName, familyName, phone, affiliation, country
     * 
     * @return List of authors with complete information
     */
    public List<AuthorListDto> getAllAuthors() {
        log.info("Fetching all authors with details");
        
        // Step 1: Get all distinct authors from authors table
        List<Author> authors = authorRepository.findAllDistinctAuthors();
        
        if (authors.isEmpty()) {
            log.info("No authors found in database");
            return List.of();
        }
        
        log.info("Found {} authors in database", authors.size());
        
        // Step 2: Get author IDs for bulk fetch
        List<Long> authorIds = authors.stream()
                .map(Author::getAuthorId)
                .toList();
        
        // Step 3: Fetch author settings (names, phone, affiliation, country)
        List<String> settingNames = List.of(
            "givenName",      // First name
            "familyName",     // Last name
            "phone",          // Phone number
            "affiliation",    // Institution/Affiliation
            "country"         // Country
        );
        
        List<AuthorSetting> authorSettings = authorSettingRepository
                .findByAuthorIdInAndSettingNameIn(authorIds, settingNames);
        
        log.info("Found {} author settings entries", authorSettings.size());
        
        // Step 4: Group settings by author_id for easy lookup
        Map<Long, Map<String, String>> authorSettingsMap = authorSettings.stream()
                .collect(Collectors.groupingBy(
                    AuthorSetting::getAuthorId,
                    Collectors.toMap(
                        AuthorSetting::getSettingName,
                        as -> as.getSettingValue() != null ? as.getSettingValue() : "",
                        (existing, replacement) -> existing // Keep first locale if duplicates
                    )
                ));
        
        // Step 5: Build AuthorListDto for each author
        List<AuthorListDto> authorList = new ArrayList<>();
        
        for (Author author : authors) {
            Map<String, String> settings = authorSettingsMap.getOrDefault(
                author.getAuthorId(), 
                Map.of()
            );
            
            String givenName = settings.getOrDefault("givenName", "");
            String familyName = settings.getOrDefault("familyName", "");
            String fullName = buildFullName(givenName, familyName);
            String phone = settings.getOrDefault("phone", "");
            String affiliation = settings.getOrDefault("affiliation", "");
            String country = settings.getOrDefault("country", "");
            
            authorList.add(AuthorListDto.builder()
                    .authorId(author.getAuthorId())
                    .givenName(givenName)
                    .familyName(familyName)
                    .fullName(fullName)
                    .email(author.getEmail())
                    .phone(phone)
                    .affiliation(affiliation)
                    .country(country)
                    .build());
        }
        
        log.info("Successfully built author list with {} entries", authorList.size());
        return authorList;
    }
    
    /**
     * Get author by ID with complete details
     * 
     * @param authorId The author's unique identifier
     * @return Author details or null if not found
     */
    public AuthorListDto getAuthorById(Long authorId) {
        log.info("Fetching author details for authorId: {}", authorId);
        
        // Fetch author from database
        Author author = authorRepository.findById(authorId).orElse(null);
        
        if (author == null) {
            log.warn("Author not found with id: {}", authorId);
            return null;
        }
        
        // Fetch author settings
        List<String> settingNames = List.of("givenName", "familyName", "phone", "affiliation", "country");
        List<AuthorSetting> authorSettings = authorSettingRepository
                .findByAuthorIdInAndSettingNameIn(List.of(authorId), settingNames);
        
        // Map settings
        Map<String, String> settings = authorSettings.stream()
                .collect(Collectors.toMap(
                    AuthorSetting::getSettingName,
                    as -> as.getSettingValue() != null ? as.getSettingValue() : "",
                    (existing, replacement) -> existing
                ));
        
        String givenName = settings.getOrDefault("givenName", "");
        String familyName = settings.getOrDefault("familyName", "");
        
        return AuthorListDto.builder()
                .authorId(author.getAuthorId())
                .givenName(givenName)
                .familyName(familyName)
                .fullName(buildFullName(givenName, familyName))
                .email(author.getEmail())
                .phone(settings.getOrDefault("phone", ""))
                .affiliation(settings.getOrDefault("affiliation", ""))
                .country(settings.getOrDefault("country", ""))
                .build();
    }
    
    /**
     * Helper method to build full name from given name and family name
     */
    private String buildFullName(String givenName, String familyName) {
        String fullName = (givenName + " " + familyName).trim();
        return fullName.isEmpty() ? "Unknown Author" : fullName;
    }
}
