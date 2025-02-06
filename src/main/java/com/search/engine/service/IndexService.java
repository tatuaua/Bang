package com.search.engine.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.search.engine.model.PageOccurrences;
import com.search.engine.model.Word;
import com.search.engine.util.Database;

@Service
public class IndexService {

    private static final Set<String> STOP_WORDS = Set.of(
        "the", "is", "in", "at", "of", "and", "a", "to"
    );

    public void updateIndex(MultipartFile multipartFile) throws IOException {

        System.out.println("Updating index with file: " + multipartFile.getOriginalFilename());

        List<Word> wordList = new ArrayList<>();
        
        String text = null;

        text = new String(multipartFile.getBytes());
        
        String documentName = multipartFile.getOriginalFilename();
        String[] words = text.split("\\s+");

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

        Database.open();
        Database.updateIndex(wordList);
        Database.close();
    }


}
