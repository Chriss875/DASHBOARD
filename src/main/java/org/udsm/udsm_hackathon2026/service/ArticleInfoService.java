package org.udsm.udsm_hackathon2026.service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.udsm.udsm_hackathon2026.dto.ArticleInfoDto;
import org.udsm.udsm_hackathon2026.model.Author;
import org.udsm.udsm_hackathon2026.model.AuthorSetting;
import org.udsm.udsm_hackathon2026.model.PublicationSetting;
import org.udsm.udsm_hackathon2026.repository.AuthorRepository;
import org.udsm.udsm_hackathon2026.repository.AuthorSettingRepository;
import org.udsm.udsm_hackathon2026.repository.PublicationSettingRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleInfoService {

    private final PublicationSettingRepository publicationSettingRepo;
    private final AuthorRepository authorRepo;
    private final AuthorSettingRepository authorSettingRepo;

    /**
     * Fetches article title, abstract, and author names from OJS tables.
     * publication_id in OJS is the articleId we track.
     */
    public ArticleInfoDto getArticleInfo(Long publicationId) {
        // 1. Get title and abstract from publication_settings
        List<PublicationSetting> settings = publicationSettingRepo
                .findByPublicationIdAndSettingNameIn(publicationId, List.of("title", "abstract"));

        String title = null;
        String articleAbstract = null;
        for (PublicationSetting s : settings) {
            if ("title".equals(s.getSettingName()) && title == null) {
                title = s.getSettingValue();
            }
            if ("abstract".equals(s.getSettingName()) && articleAbstract == null) {
                articleAbstract = s.getSettingValue();
            }
        }

        // 2. Get authors ordered by sequence
        List<Author> authors = authorRepo.findByPublicationIdOrderBySeqAsc(publicationId);
        List<String> authorNames = new ArrayList<>();

        if (!authors.isEmpty()) {
            List<Long> authorIds = authors.stream().map(Author::getAuthorId).toList();

            // 3. Get author givenName and familyName from author_settings
            List<AuthorSetting> authorSettings = authorSettingRepo
                    .findByAuthorIdInAndSettingNameIn(authorIds, List.of("givenName", "familyName"));

            // Group by authorId
            Map<Long, Map<String, String>> authorSettingsMap = authorSettings.stream()
                    .collect(Collectors.groupingBy(
                            AuthorSetting::getAuthorId,
                            Collectors.toMap(
                                    AuthorSetting::getSettingName,
                                    as -> as.getSettingValue() != null ? as.getSettingValue() : "",
                                    (existing, replacement) -> existing // keep first locale
                            )
                    ));

            for (Author author : authors) {
                Map<String, String> nameMap = authorSettingsMap.getOrDefault(author.getAuthorId(), Map.of());
                String given = nameMap.getOrDefault("givenName", "");
                String family = nameMap.getOrDefault("familyName", "");
                String fullName = (given + " " + family).trim();
                if (!fullName.isEmpty()) {
                    authorNames.add(fullName);
                }
            }
        }

        return ArticleInfoDto.builder()
                .articleId(publicationId)
                .title(title)
                .articleAbstract(articleAbstract)
                .authors(authorNames)
                .build();
    }
}
