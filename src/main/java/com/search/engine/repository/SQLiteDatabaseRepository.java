package com.search.engine.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.stereotype.Repository;

import com.search.engine.model.PageOccurrences;
import com.search.engine.model.Word;

@Repository
public class SQLiteDatabaseRepository implements DatabaseRepository {

    public static boolean INITIALIZED = false;

    private static final String DEFAULT_DB_PATH = "jdbc:sqlite:src/main/resources/index.db";
    private static final String ENV_DB_PATH = System.getenv("SPRING_DATASOURCE_URL");
    private static final String URL = ENV_DB_PATH != null 
            ? ENV_DB_PATH
            : DEFAULT_DB_PATH;

    private static Connection connection;
    
    private void connect() {
        try {
           connection = DriverManager.getConnection(URL);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void open() {
        connect();
    }

    public void init() {

        if (INITIALIZED) {
            return;
        }

        System.out.println("Initializing database...");

        INITIALIZED = true;

        connect();

        String[] sqlStatements = {
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

        try {
            for (String sql : sqlStatements) {
                connection.createStatement().execute(sql);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Database initialized.");

        close();
    }

    public void upsertIndex(List<Word> index) {
        
        List<String> words = index.stream().map(Word::getWord).toList();

        batchInsertWords(words);

        for (Word word : index) {
            batchInsertOccurrences(word.getWord(), word.getPageOccurrences());
        }
    }

    public void batchInsertOccurrences(String word, List<PageOccurrences> occurrences) {

        try {
            connection.setAutoCommit(false);
            for (PageOccurrences occurrence : occurrences) {
                insertPageOccurrences(word, occurrence.getPage(), occurrence.getOccurrences());
            }
            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void batchInsertWords(List<String> words) {

        try {
            connection.setAutoCommit(false);
            for (String word : words) {
                insertWordIfAbsent(word);
            }
            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void insertWordIfAbsent(String word) {

        String sql = String.format("""
            INSERT INTO Words (word)
            VALUES ('%s')
            ON CONFLICT(word) DO NOTHING;
            """, word);
        try {
            connection.createStatement().execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void insertPageOccurrencesIfAbsent(String word, String documentName, int occurrences) {
        
        if (getPageOccurrences(word, documentName) == null) {
            insertPageOccurrences(word, documentName, occurrences);
        }
    }

    public void insertPageOccurrences(String word, String documentName, int occurrences) {

        String sql = String.format("""
            INSERT INTO Occurrences (word_id, document_name, occurrences)
            VALUES (
                (SELECT word_id FROM Words WHERE word = '%s'),
                '%s',
                %d
            );
            """, word, documentName, occurrences);
        try {
            connection.createStatement().execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public PageOccurrences getPageOccurrences(String word, String documentName) {

        String sql = String.format("""
            SELECT occurrences
            FROM Occurrences
            WHERE word_id = (SELECT word_id FROM Words WHERE word = '%s')
            AND document_name = '%s';
            """, word, documentName);
        try {
            ResultSet resultSet = connection.createStatement().executeQuery(sql);
            if (resultSet.next()) {
                return new PageOccurrences(documentName, resultSet.getInt("occurrences"));
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<PageOccurrences> getTop5Documents(String word) {

        String sql = String.format("""
            SELECT o.document_name, SUM(o.occurrences) AS total_occurrences
            FROM Occurrences o
            JOIN Words w ON o.word_id = w.word_id
            WHERE w.word = '%s'
            GROUP BY o.document_name
            ORDER BY total_occurrences DESC
            LIMIT 5;
            """, word);

        List<PageOccurrences> result = new ArrayList<>();

        try {
            ResultSet resultSet = connection.createStatement().executeQuery(sql);

            if (!resultSet.isBeforeFirst()) {

                if (IsDatabaseEmpty()) { // If database is empty, return empty list
                    return result;
                }

                String lowestDistanceWord = getLowestDistanceWord(word); // If word doesnt exist in our index, search for the closest word and return its top 5 documents
                return getTop5Documents(lowestDistanceWord);
            }

            while (resultSet.next()) {
                result.add(new PageOccurrences(resultSet.getString("document_name"), resultSet.getInt("total_occurrences")));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean IsDatabaseEmpty() {

        String sql = """
            SELECT COUNT(*) AS count
            FROM Words;
            """;

        try {
            ResultSet resultSet = connection.createStatement().executeQuery(sql);
            if (resultSet.next()) {
                return resultSet.getInt("count") == 0;
            }
            return true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String getLowestDistanceWord(String word) {

        String sql = """
            SELECT w.word
            FROM Words w;
            """;

        List<String> words = new ArrayList<>();
        try {
            ResultSet resultSet = connection.createStatement().executeQuery(sql);
            while (resultSet.next()) {
                words.add(resultSet.getString("word"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        LevenshteinDistance levenshteinDistance = LevenshteinDistance.getDefaultInstance();

        int lowestDistance = Integer.MAX_VALUE;
        String lowestDistanceWord = null;

        for (String w : words) {
            int distance = levenshteinDistance.apply(w, word);
            if (distance <= lowestDistance) {
                lowestDistance = distance;
                lowestDistanceWord = w;
            }
        }

        System.out.println("No exact match found for word: " + word + ". Closest match found: " + lowestDistanceWord);
        return lowestDistanceWord;
    }
    
}
