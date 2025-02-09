package com.search.engine.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.search.engine.model.Word;
import com.search.engine.service.SearchService;

@RestController
@RequestMapping("/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @PostMapping()
    public ResponseEntity<?> search(@RequestParam List<String> q) throws IOException {

        System.out.println("q: " + q);

        List<Word> result = new ArrayList<>();

        try {
            result = searchService.getTop5Documents(q);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (result.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}