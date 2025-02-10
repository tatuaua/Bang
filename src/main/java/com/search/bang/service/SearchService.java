package com.search.bang.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.search.bang.model.PageOccurrences;
import com.search.bang.model.Word;
import com.search.bang.repository.DatabaseRepository;

@Service
public class SearchService {

    private final DatabaseRepository databaseRepository;

    public SearchService(DatabaseRepository databaseRepository) {
        this.databaseRepository = databaseRepository;
    }

    public List<Word> getTop5Documents(List<String> words) {
        List<Word> result = new ArrayList<>();
        for (String word : words) {
            List<PageOccurrences> occurrences = databaseRepository.getTop5Documents(word, true);
            occurrences.sort((a, b) -> Integer.compare(b.getAmount(), a.getAmount()));
            Word newWord = new Word();
            newWord.setWord(word);
            newWord.setPageOccurrences(occurrences);
            result.add(newWord);
        }
        return result;
    }
}
