package com.search.engine.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.search.engine.model.PageOccurrences;
import com.search.engine.model.Word;
import com.search.engine.repository.DatabaseRepository;

@Service
public class SearchService {

    DatabaseRepository databaseRepository;

    public SearchService(DatabaseRepository databaseRepository) {
        this.databaseRepository = databaseRepository;
    }

    public List<Word> getTop5Documents(List<String> words) {
        List<Word> result = new ArrayList<>();
        for(String word : words) {
            List<PageOccurrences> occurrences = databaseRepository.getTop5Documents(word, true);
            occurrences.sort((a, b) -> Integer.compare(b.getOccurrences(), a.getOccurrences()));
            Word newWord = new Word();
            newWord.setWord(word);
            newWord.setPageOccurrences(occurrences);
            result.add(newWord);
        }
        return result;
    }
}
