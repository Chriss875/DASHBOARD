package org.udsm.udsm_hackathon2026.service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.udsm.udsm_hackathon2026.dto.CitationResponse;
import org.udsm.udsm_hackathon2026.dto.CrossrefResponse;
import org.udsm.udsm_hackathon2026.model.Publication;
import org.udsm.udsm_hackathon2026.model.PublicationCitationHistory;
import org.udsm.udsm_hackathon2026.repository.PublicationCitationRepositoryHistory;
import org.udsm.udsm_hackathon2026.repository.PublicationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@Slf4j
@RequiredArgsConstructor
public class CrossrefCitationService {
    private final PublicationRepository publicationRepository;
    private final PublicationCitationRepositoryHistory historyRepository;
    private final RestTemplate restTemplate;

    @Value("${crossref.api.url:https://api.crossref.org/works}")
    private String crossrefApiUrl;

    @Value("${crossref.api.email:christophermtoi@gmail.com}")
    private String contactEmail;


    /**
     * Fetch citation count from Crossref API for a specific DOI
     */
    public Optional<Integer> fetchCitationCountFromCrossref(String doi) {
        if (doi == null || doi.trim().isEmpty()) {
            log.warn("DOI is null or empty");
            return Optional.empty();
        }

        try {
            String url = String.format("%s/%s", crossrefApiUrl, doi.trim());

            restTemplate.getInterceptors().add((request, body, execution) -> {
                request.getHeaders().add("User-Agent",
                        String.format("CitationTracker/1.0 (mailto:%s)", contactEmail));
                return execution.execute(request, body);
            });

            log.info("Fetching citation count for DOI: {}", doi);
            ResponseEntity<CrossrefResponse> response =
                    restTemplate.getForEntity(url, CrossrefResponse.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                CrossrefResponse crossrefResponse = response.getBody();
                Integer citationCount = crossrefResponse.getMessage().getIsReferencedByCount();

                if (citationCount == null) {
                    citationCount = 0;
                }

                log.info("DOI {} has {} citations", doi, citationCount);
                return Optional.of(citationCount);
            }

            log.warn("Unexpected response status: {}", response.getStatusCode());
            return Optional.empty();

        } catch (RestClientException e) {
            log.error("Error fetching citation count for DOI {}: {}", doi, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Update citation count for a specific publication
     */
    /**
     * Smart method: Get citation count, auto-update if stale
     * - If checked within last 24 hours: return cached data
     * - If older or never checked: fetch from Crossref
     */
    @Transactional
    public CitationResponse getOrUpdateCitationCount(Long publicationId) {
        Optional<Publication> publicationOpt = publicationRepository.findById(publicationId);

        if (publicationOpt.isEmpty()) {
            return CitationResponse.builder()
                    .publicationId(publicationId)
                    .status("ERROR")
                    .message("Publication not found")
                    .build();
        }

        Publication publication = publicationOpt.get();

        if (publication.getDoi() == null || publication.getDoi().isEmpty()) {
            return CitationResponse.builder()
                    .publicationId(publicationId)
                    .status("ERROR")
                    .message("No DOI found for this publication")
                    .build();
        }

        // Check if data is fresh (less than 24 hours old)
        boolean isDataFresh = publication.getLastCitationCheck() != null &&
                publication.getLastCitationCheck()
                        .isAfter(LocalDateTime.now().minusHours(24));

        if (isDataFresh) {
            // Return cached data (no API call needed)
            log.info("Returning cached citation count for publication {}", publicationId);
            return CitationResponse.builder()
                    .publicationId(publication.getPublicationId())
                    .doi(publication.getDoi())
                    .citationCount(publication.getCitationCount())
                    .lastChecked(publication.getLastCitationCheck())
                    .status("SUCCESS")
                    .message("Citation count (cached)")
                    .build();
        } else {
            // Data is stale, update from Crossref
            log.info("Data is stale, updating from Crossref for publication {}", publicationId);
            return updatePublicationCitations(publication);
        }
    }

    /**
     * Force update (keep for admin use only)
     */
    @Transactional
    public CitationResponse forceUpdateCitationCount(Long publicationId) {
        Optional<Publication> publicationOpt = publicationRepository.findById(publicationId);

        if (publicationOpt.isEmpty()) {
            return CitationResponse.builder()
                    .publicationId(publicationId)
                    .status("ERROR")
                    .message("Publication not found")
                    .build();
        }

        Publication publication = publicationOpt.get();

        if (publication.getDoi() == null || publication.getDoi().isEmpty()) {
            return CitationResponse.builder()
                    .publicationId(publicationId)
                    .status("ERROR")
                    .message("No DOI found for this publication")
                    .build();
        }

        return updatePublicationCitations(publication);
    }

    /**
     * Update citation count for a publication by DOI
     */
    @Transactional
    public CitationResponse updateCitationCountByDoi(String doi) {
        Optional<Publication> publicationOpt = publicationRepository.findByDoi(doi);

        if (publicationOpt.isEmpty()) {
            return CitationResponse.builder()
                    .doi(doi)
                    .status("ERROR")
                    .message("Publication with DOI not found in database")
                    .build();
        }

        return updatePublicationCitations(publicationOpt.get());
    }

    /**
     * Internal method to update publication citations
     */
    private CitationResponse updatePublicationCitations(Publication publication) {
        Integer previousCount = publication.getCitationCount() != null ?
                publication.getCitationCount() : 0;

        Optional<Integer> citationCountOpt = fetchCitationCountFromCrossref(publication.getDoi());

        if (citationCountOpt.isEmpty()) {
            return CitationResponse.builder()
                    .publicationId(publication.getPublicationId())
                    .doi(publication.getDoi())
                    .citationCount(previousCount)
                    .status("ERROR")
                    .message("Failed to fetch citation count from Crossref")
                    .build();
        }

        Integer newCount = citationCountOpt.get();
        Integer increase = newCount - previousCount;

        // Update publication
        publication.setCitationCount(newCount);
        publication.setLastCitationCheck(LocalDateTime.now());
        publicationRepository.save(publication);

        // Save history record
        PublicationCitationHistory history = new PublicationCitationHistory();
        history.setPublicationId(publication.getPublicationId());
        history.setCitationCount(newCount);
        history.setPreviousCount(previousCount);
        history.setCheckedAt(LocalDateTime.now());
        history.setSource("crossref");
        historyRepository.save(history);

        log.info("Updated publication {} - Citations: {} ({}{})",
                publication.getPublicationId(), newCount,
                increase >= 0 ? "+" : "", increase);

        return CitationResponse.builder()
                .publicationId(publication.getPublicationId())
                .doi(publication.getDoi())
                .citationCount(newCount)
                .previousCount(previousCount)
                .citationIncrease(increase)
                .lastChecked(LocalDateTime.now())
                .status("SUCCESS")
                .message(increase > 0 ?
                        String.format("New citations detected: +%d", increase) :
                        "No new citations")
                .build();
    }

    /**
     * Update all publications with DOIs
     */
    @Transactional
    public List<CitationResponse> updateAllPublications() {
        List<Publication> publications = publicationRepository.findAllWithDoi();
        List<CitationResponse> responses = new ArrayList<>();

        log.info("Starting citation update for {} publications", publications.size());

        for (Publication publication : publications) {
            try {
                CitationResponse response = updatePublicationCitations(publication);
                responses.add(response);

                // Rate limiting: wait 1 second between requests to be polite
                Thread.sleep(1000);

            } catch (InterruptedException e) {
                log.error("Update interrupted", e);
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Error updating publication {}: {}",
                        publication.getPublicationId(), e.getMessage());

                responses.add(CitationResponse.builder()
                        .publicationId(publication.getPublicationId())
                        .doi(publication.getDoi())
                        .status("ERROR")
                        .message(e.getMessage())
                        .build());
            }
        }

        log.info("Citation update complete. Processed {} publications", responses.size());
        return responses;
    }

    /**
     * Update publications that haven't been checked in specified hours
     */
    @Transactional
    public List<CitationResponse> updateStalePublications(int hoursOld) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(hoursOld);
        List<Publication> publications = publicationRepository.findPublicationsNeedingUpdate(cutoffTime);
        List<CitationResponse> responses = new ArrayList<>();

        log.info("Found {} publications needing update (older than {} hours)",
                publications.size(), hoursOld);

        for (Publication publication : publications) {
            try {
                CitationResponse response = updatePublicationCitations(publication);
                responses.add(response);
                Thread.sleep(1000); // Rate limiting

            } catch (InterruptedException e) {
                log.error("Update interrupted", e);
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Error updating publication {}: {}",
                        publication.getPublicationId(), e.getMessage());
            }
        }

        return responses;
    }

    /**
     * Get citation history for a publication
     */
    public List<PublicationCitationHistory> getCitationHistory(Long publicationId) {
        return historyRepository.findByPublicationIdOrderByCheckedAtDesc(publicationId);
    }

    /**
     * Get recent citation increases across all publications
     */
    public List<PublicationCitationHistory> getRecentCitationIncreases() {
        return historyRepository.findRecentCitationIncreases();
    }
}
