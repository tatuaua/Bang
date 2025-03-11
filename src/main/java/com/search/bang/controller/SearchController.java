package com.search.bang.controller;

import java.io.IOException;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.search.bang.model.Word;
import com.search.bang.service.SearchService;

@Slf4j
@RestController
@RequestMapping("/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @PostMapping()
    public ResponseEntity<?> search(
            @RequestParam(required = false) Boolean exact,
            @RequestParam List<String> q
    ) throws IOException {
        log.info("Query received: {}", q);

        List<Word> result;

        try {
            if (exact != null && exact) {
                result = searchService.getTop5DocumentsExact(q);
            } else {
                result = searchService.getTop5DocumentsFuzzy(q);
            }
        } catch (Exception e) {
            log.error("Error encountered while searching: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (result.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}