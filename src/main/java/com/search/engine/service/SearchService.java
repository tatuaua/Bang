package com.search.engine.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.search.engine.model.PageOccurrences;
import com.search.engine.model.Word;
import com.search.engine.util.Database;

@Service
public class SearchService {

    public List<Word> getTop5Documents(List<String> words) {
        List<Word> result = new ArrayList<>();
        Database.open();
        for(String word : words) {
            List<PageOccurrences> occurrences = Database.getTop5Documents(word);
            occurrences.sort((a, b) -> Integer.compare(b.getOccurrences(), a.getOccurrences()));
            Word newWord = new Word();
            newWord.setWord(word);
            newWord.setPageOccurrences(occurrences);
            result.add(newWord);
        }
        Database.close();
        return result;
    }
    
}
