package com.search.engine.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.search.engine.model.Word;
import com.search.engine.service.SearchService;
@RestController
@RequestMapping("/search")
public class SearchController {

    SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @PostMapping()
    public List<Word> search(@RequestParam List<String> q) throws IOException {

        System.out.println("q: " + q);
        return searchService.getTop5Documents(q);
    }
}