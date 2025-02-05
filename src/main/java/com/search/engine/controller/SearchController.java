package com.search.engine.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.search.engine.model.PageOccurrences;
import com.search.engine.model.Word;
import com.search.engine.util.Database;

@RestController
@RequestMapping("/search")
public class SearchController {

    @PostMapping()
    public List<Word> search(@RequestParam List<String> q) throws IOException {

        System.out.println("q: " + q);
        Database.open();
        List<Word> result = new ArrayList<>();
        for(String word : q) {
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