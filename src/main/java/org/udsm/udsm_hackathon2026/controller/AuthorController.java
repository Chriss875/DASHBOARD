package org.udsm.udsm_hackathon2026.controller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.udsm.udsm_hackathon2026.dto.author.AuthorListDto;
import org.udsm.udsm_hackathon2026.service.AuthorListService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/authors")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Authors", description = "Author management and listing endpoints")
public class AuthorController {

    private final AuthorListService authorListService;

    /**
     * Get all authors with their complete details
     * 
     * Endpoint: GET /api/v1/authors
     * 
     * Returns a list of all authors in the system with their:
     * - Full name (first name + last name)
     * - Email address
     * - Affiliation/Institution
     * - Country
     */
    @GetMapping
    @Operation(
        summary = "Get all authors",
        description = "Retrieves a complete list of all authors in the system with their contact details. " +
                     "Data is fetched from the OJS authors and author_settings tables. " +
                     "Includes name, email, affiliation, and country information."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved list of authors",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = AuthorListDto.class))
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(mediaType = "application/json")
        )
    })
    public ResponseEntity<List<AuthorListDto>> getAllAuthors() {
        log.info("GET /api/v1/authors - Fetching all authors");
        
        try {
            List<AuthorListDto> authors = authorListService.getAllAuthors();
            log.info("Successfully retrieved {} authors", authors.size());
            return ResponseEntity.ok(authors);
            
        } catch (Exception e) {
            log.error("Error fetching authors", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get a specific author by ID
     * 
     * Endpoint: GET /api/v1/authors/{authorId}
     * 
     * Returns detailed information for a single author
     */
    @GetMapping("/{authorId}")
    @Operation(
        summary = "Get author by ID",
        description = "Retrieves detailed information for a specific author by their unique ID. " +
                     "Returns author's name, email, affiliation, and country."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved author details",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthorListDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Author not found",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(mediaType = "application/json")
        )
    })
    public ResponseEntity<AuthorListDto> getAuthorById(
            @Parameter(description = "Unique author identifier", example = "12345", required = true)
            @PathVariable Long authorId) {
        
        log.info("GET /api/v1/authors/{} - Fetching author details", authorId);
        
        try {
            AuthorListDto author = authorListService.getAuthorById(authorId);
            
            if (author == null) {
                log.warn("Author not found with id: {}", authorId);
                return ResponseEntity.notFound().build();
            }
            
            log.info("Successfully retrieved author: {}", author.getFullName());
            return ResponseEntity.ok(author);
            
        } catch (Exception e) {
            log.error("Error fetching author with id: {}", authorId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
