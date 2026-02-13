package org.udsm.udsm_hackathon2026.repository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Article Repository using JdbcTemplate for native queries
 * No JPA entity needed since we're using raw SQL
 */
@Repository
public class ArticleRepository {

    private final JdbcTemplate jdbcTemplate;

    public ArticleRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Get all articles with ID and name (title) only
     * No categories since database doesn't have them set up
     */
    public List<Object[]> findAllArticlesForListing() {
        String sql = "SELECT " +
                "s.submission_id AS id, " +
                "ps.setting_value AS name " +
                "FROM submissions s " +
                "JOIN publications p ON s.current_publication_id = p.publication_id " +
                "JOIN publication_settings ps ON p.publication_id = ps.publication_id " +
                "    AND ps.setting_name = 'title' AND ps.locale = 'en_US' " +
                "WHERE s.status = 3 " +
                "ORDER BY ps.setting_value";

        return jdbcTemplate.query(sql, (rs, rowNum) -> new Object[] {
                rs.getLong("id"),
                rs.getString("name")
        });
    }

    /**
     * Get complete article information with abstract
     */
    public List<Object[]> findArticleDetailsById(Long articleId) {
        String sql = "SELECT " +
                "s.submission_id AS id, " +
                "MAX(CASE WHEN ps.setting_name = 'title' THEN ps.setting_value END) AS name, " +
                "MAX(CASE WHEN ps.setting_name = 'abstract' THEN ps.setting_value END) AS abstract, " +
                "p.date_published, " +
                "p.publication_id " +
                "FROM submissions s " +
                "JOIN publications p ON s.current_publication_id = p.publication_id " +
                "LEFT JOIN publication_settings ps ON p.publication_id = ps.publication_id " +
                "    AND ps.locale = 'en_US' " +
                "WHERE s.submission_id = ? " +
                "AND s.status = 3 " +
                "GROUP BY s.submission_id, p.date_published, p.publication_id";

        return jdbcTemplate.query(sql, (rs, rowNum) -> new Object[] {
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("abstract"),
                rs.getDate("date_published"),
                rs.getLong("publication_id")
        }, articleId);
    }

    /**
     * Get all author names for an article
     */
    public List<String> findAuthorNamesByPublicationId(Long publicationId) {
        String sql = "SELECT " +
                "COALESCE( " +
                "    MAX(CASE WHEN aus.setting_name = 'preferredPublicName' THEN aus.setting_value END), " +
                "    CONCAT( " +
                "        COALESCE(MAX(CASE WHEN aus.setting_name = 'givenName' THEN aus.setting_value END), ''), " +
                "        ' ', " +
                "        COALESCE(MAX(CASE WHEN aus.setting_name = 'familyName' THEN aus.setting_value END), '') " +
                "    ) " +
                ") AS author_name " +
                "FROM authors a " +
                "LEFT JOIN author_settings aus ON a.author_id = aus.author_id " +
                "WHERE a.publication_id = ? " +
                "AND a.include_in_browse = 1 " +
                "GROUP BY a.author_id, a.seq " +
                "ORDER BY a.seq";

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                rs.getString("author_name"), publicationId);
    }
}