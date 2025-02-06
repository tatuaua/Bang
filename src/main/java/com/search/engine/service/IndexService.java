package com.search.engine.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import com.search.engine.model.PageOccurrences;
import com.search.engine.model.Word;
import com.search.engine.util.Database;

import jakarta.annotation.PostConstruct;

import static com.search.engine.util.Constants.*;

@Service
public class IndexService {

    @PostConstruct
    public void createIndexOnStartup() throws IOException {
        System.out.println("Indexing started...");
        createIndex();
        System.out.println("Indexing completed!");
    }

    public void createIndex() throws IOException {

        File directory = new File(DIRECTORY_PATH);
        File[] files = directory.listFiles();
        if (files == null || files.length == 0) {
            throw new RuntimeException("No files found in the input directory");
        }

        List<Word> wordList = new ArrayList<>();

        for (File file : files) {

            System.out.println("Indexing file: " + file.getName());
            
            String documentName = file.getName();
            String text = Files.readString(file.toPath());
            String[] words = text.split("\\s+");
            
            List.of(words).forEach(s -> s = s.toLowerCase());

            for (String word : words) {
                if (STOP_WORDS.contains(word)) {
                    continue;
                }

                Word existingWord = wordList.stream()
                        .filter(w -> w.getWord().equals(word))
                        .findFirst()
                        .orElse(null);

                if (Objects.isNull(existingWord)) {
                    Word newWord = new Word();
                    newWord.setWord(word);
                    newWord.addPageOccurrence(documentName);
                    wordList.add(newWord);

                } else {
                    PageOccurrences existingPageOccurrences = existingWord.getPageOccurrences().stream()
                            .filter(po -> po.getPage().equals(documentName))
                            .findFirst()
                            .orElse(null);

                    if (Objects.isNull(existingPageOccurrences)) {
                        existingWord.addPageOccurrence(documentName);
                    } else {
                        existingPageOccurrences.setOccurrences(existingPageOccurrences.getOccurrences() + 1);
                    }
                }
            }
        }

        Database.init();
        System.out.println("Inserting index into database...");
        Database.insertIndex(wordList);
        System.out.println("Index inserted successfully!");
        Database.close();
    }

    /*public void updateIndex(MultipartFile multipartFile) {

        List<Word> wordList = new ArrayList<>();
        
        String text = null;

        try {
            text = new String(multipartFile.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String documentName = multipartFile.getOriginalFilename();
        String[] words = text.split("\\s+");

        for (String word : words) {
            if (STOP_WORDS.contains(word)) {
                continue;
            }

            Word existingWord = wordList.stream()
                    .filter(w -> w.getWord().equals(word))
                    .findFirst()
                    .orElse(null);

            if (Objects.isNull(existingWord)) {
                Word newWord = new Word();
                newWord.setWord(word);
                newWord.addPageOccurrence(documentName);
                wordList.add(newWord);

            } else {
                PageOccurrences existingPageOccurrences = existingWord.getPageOccurrences().stream()
                        .filter(po -> po.getPage().equals(documentName))
                        .findFirst()
                        .orElse(null);

                if (Objects.isNull(existingPageOccurrences)) {
                    existingWord.addPageOccurrence(documentName);
                } else {
                    existingPageOccurrences.setOccurrences(existingPageOccurrences.getOccurrences() + 1);
                }
            }
        }

        Database.init();
        Database.updateIndex(wordList);
        Database.close();
    }*/


}
