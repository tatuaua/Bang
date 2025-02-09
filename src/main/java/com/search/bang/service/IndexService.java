package com.search.bang.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.search.bang.model.Word;
import com.search.bang.repository.DatabaseRepository;

import static com.search.bang.util.Constants.STOP_WORDS;

@Slf4j
@Service
public class IndexService {

    private final DatabaseRepository databaseRepository;

    public IndexService(DatabaseRepository databaseRepository) {
        this.databaseRepository = databaseRepository;
    }

    public void updateIndex(MultipartFile multipartFile) throws IOException {

        log.info("Updating index with file: {}", multipartFile.getOriginalFilename());

        String text = null;

        try {
            text = new String(multipartFile.getBytes());
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read file content");
        }

        String documentName = multipartFile.getOriginalFilename();

        text = text.replace("\n", " ")
                .replaceAll("[^\\w\\s]", "")
                .replaceAll("\\s+", " ")
                .trim();

        String[] words = text.split(" ");

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

            if (existingWord == null) {
                Word newWord = new Word();
                newWord.setWord(word);
                newWord.addPageOccurrence(documentName);
                wordList.add(newWord);
            } else {
                existingWord.updatePageOccurrence(documentName);
            }
        }

        databaseRepository.upsertIndex(wordList);
        log.info("Index updated with file: {}", multipartFile.getOriginalFilename());
    }
}
