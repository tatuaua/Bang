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
    
    private void open() {
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

    public void init() {

        if (INITIALIZED) {
            System.out.println("Database already initialized.");
            return;
        }

        System.out.println("Initializing database...");

        INITIALIZED = true;

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

        open();

        try {
            for (String sql : sqlStatements) {
                connection.createStatement().execute(sql);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        close();

        System.out.println("Database initialized.");
    }

    public synchronized void upsertIndex(List<Word> index) {
        open();
        List<String> words = index.stream().map(Word::getWord).toList();

        batchInsertWords(words);

        for (Word word : index) {
            batchInsertOccurrences(word.getWord(), word.getPageOccurrences());
        }
        close();
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

    public synchronized List<PageOccurrences> getTop5Documents(String word, boolean isOriginalWord) {

        if (isOriginalWord && !wordExistsInDatabase(word)) { // If word doesnt exist in our index, search for the closest word and return its top 5 documents
            String lowestDistanceWord = getLowestDistanceWord(word);
            return getTop5Documents(lowestDistanceWord, false);
        }

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

        open();
        ResultSet resultSet = executeQuery(sql);

        try {
            while (resultSet.next()) {
                result.add(new PageOccurrences(resultSet.getString("document_name"), resultSet.getInt("total_occurrences")));
            }
            close();
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean wordExistsInDatabase(String word) {
        
        String sql = String.format("""
            SELECT w.word
            FROM Words w
            WHERE w.word = '%s';
            """, word);

        open();
        ResultSet resultSet = executeQuery(sql);

        try {
            boolean result = resultSet.isBeforeFirst();
            close();
            return result;
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

        open();
        ResultSet resultSet = executeQuery(sql);

        try {
            while (resultSet.next()) {
                words.add(resultSet.getString("word"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        close();

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

    private ResultSet executeQuery(String sql) {
        try {
            ResultSet result = connection.createStatement().executeQuery(sql);
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
