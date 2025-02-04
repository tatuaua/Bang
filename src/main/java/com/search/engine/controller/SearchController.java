package com.search.engine.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.search.engine.model.PageOccurrences;
import com.search.engine.util.Database;

@RestController
@RequestMapping("/search")
public class SearchController {

    @GetMapping("/{query}")
    public List<PageOccurrences> search(@PathVariable String query) throws IOException {
        Database.open();
        List<PageOccurrences> result = Database.getTop5Documents(query);
        Database.close();
        result.sort((a, b) -> Integer.compare(b.getOccurrences(), a.getOccurrences()));
        return result;
    }
}