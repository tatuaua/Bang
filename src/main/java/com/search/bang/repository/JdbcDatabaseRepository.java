package com.search.bang.repository;

import com.search.bang.model.PageOccurrences;
import com.search.bang.model.Word;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Repository
public class JdbcDatabaseRepository implements DatabaseRepository {

    private static boolean INITIALIZED = false;
    private final JdbcTemplate jdbcTemplate;

    public JdbcDatabaseRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void init() {

        if(INITIALIZED) {
            log.warn("Tried to init database twice");
        }

        log.info("Initializing databse...");

        String[] sql = new String[]{
                "DROP TABLE IF EXISTS Words;",
                "DROP TABLE IF EXISTS Occurrences;",
                """
                CREATE TABLE Words (
                    word_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    word TEXT UNIQUE NOT NULL
                );
                """,
                """
                CREATE TABLE Occurrences (
                    occurrence_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    word_id INTEGER,
                    document_name TEXT NOT NULL,
                    occurrences INTEGER NOT NULL,
                    FOREIGN KEY (word_id) REFERENCES Words(word_id) ON DELETE CASCADE
                );
                """
        };

        Arrays.stream(sql).forEach(jdbcTemplate::execute);

        log.info("Database initialized");
        INITIALIZED = true;
    }

    @Override
    public void upsertIndex(List<Word> index) {

        List<String> words = index.stream().map(Word::getWord).toList();

        batchInsertWords(words);

        for (Word word : index) {
            batchInsertOccurrences(word.getWord(), word.getPageOccurrences());
        }
    }

    private void batchInsertWords(List<String> words) {

        String sql = "INSERT INTO Words (word) VALUES (?) ON CONFLICT(word) DO NOTHING";

        jdbcTemplate.batchUpdate(sql, words, words.size(), (ps, word) -> {
            ps.setString(1, word);
        });
    }

    private void batchInsertOccurrences(String word, List<PageOccurrences> occurrences) {

        String sql = "INSERT INTO Occurrences (word_id, document_name, occurrences) VALUES ((SELECT word_id FROM Words WHERE word = ?), ?, ?)";

        jdbcTemplate.batchUpdate(sql, occurrences, occurrences.size(), (ps, occurrence) -> {
            ps.setString(1, word);
            ps.setString(2, occurrence.getPage());
            ps.setInt(3, occurrence.getAmount());
        });
    }

    @Override
    public List<PageOccurrences> getTop5Documents(String word, boolean isOriginalWord) {

        if (isOriginalWord && !wordExistsInDatabase(word)) {
            // If word doesn't exist, search for the closest word and return its top 5 documents
            String lowestDistanceWord = getLowestDistanceWord(word);
            return getTop5Documents(lowestDistanceWord, false);
        }

        String sql = """
                SELECT o.document_name, SUM(o.occurrences) AS total_occurrences
                FROM Occurrences o
                JOIN Words w ON o.word_id = w.word_id
                WHERE w.word = ?
                GROUP BY o.document_name
                ORDER BY total_occurrences DESC
                LIMIT 5;
                """;

        RowMapper<PageOccurrences> rowMapper = (rs, rowNum) -> new PageOccurrences(
                rs.getString("document_name"),
                rs.getInt("total_occurrences")
        );

        return jdbcTemplate.query(sql, rowMapper, word);
    }

    private boolean wordExistsInDatabase(String word) {

        String sql = "SELECT COUNT(*) FROM Words w WHERE w.word = ?";
        Integer rowCount = jdbcTemplate.queryForObject(sql, Integer.class, word);
        return rowCount != null && rowCount > 0;
    }

    private String getLowestDistanceWord(String word) {

        String sql = "SELECT w.word FROM Words w";
        RowMapper<String> rowMapper = (rs, rowNum) -> rs.getString("word");
        List<String> allWords = jdbcTemplate.query(sql, rowMapper);

        LevenshteinDistance levenshteinDistance = LevenshteinDistance.getDefaultInstance();
        int lowestDistance = Integer.MAX_VALUE;
        String lowestDistanceWord = null;
        for (String w : allWords) {
            int distance = levenshteinDistance.apply(w, word);
            if (distance <= lowestDistance) {
                lowestDistance = distance;
                lowestDistanceWord = w;
            }
        }

        log.debug("No exact match found for word: {} Closest match found: {}", word, lowestDistanceWord);
        return lowestDistanceWord;
    }
}
