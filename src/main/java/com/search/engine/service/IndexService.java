package com.search.engine.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.search.engine.model.Word;
import com.search.engine.repository.DatabaseRepository;

import static com.search.engine.util.Constants.STOP_WORDS;

@Service
public class IndexService {

    DatabaseRepository databaseRepository;

    public IndexService(DatabaseRepository databaseRepository) {
        this.databaseRepository = databaseRepository;
    }

    public void updateIndex(MultipartFile multipartFile) throws IOException {

        System.out.println("Updating index with file: " + multipartFile.getOriginalFilename());
        
        String text = null;

        try {
            text = new String(multipartFile.getBytes());
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read file content");
        }

        String documentName = multipartFile.getOriginalFilename();

        String cleanText = text.replace("\n", " ");
        
        // Remove all non-word characters except spaces
        cleanText = cleanText.replaceAll("[^\\w\\s]", "");
        
        // Replace multiple spaces with a single space
        cleanText = cleanText.replaceAll("\\s+", " ").trim();

        String[] words = cleanText.split(" ");

        List<Word> wordList = new ArrayList<>();

        for (int i = 0; i < words.length; i++) {

            String word = words[i].toLowerCase();

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
                
                existingWord.updatePageOccurrence(documentName);
            }
        }

        databaseRepository.open();
        databaseRepository.upsertIndex(wordList);
        databaseRepository.close();

        System.out.println("Index updated with file: " + multipartFile.getOriginalFilename());
    }
}
