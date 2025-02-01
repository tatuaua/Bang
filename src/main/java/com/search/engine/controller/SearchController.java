package com.search.engine.controller;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.search.engine.model.PageOccurrences;

import static com.search.engine.util.Constants.*;

@RestController
@RequestMapping("/search")
public class SearchController {

    @GetMapping("/{query}")
    public List<PageOccurrences> search(@PathVariable String query) throws IOException {
        Map<String, List<PageOccurrences>> index = new ObjectMapper().readValue(new File(INDEX_FILE), new TypeReference<Map<String, List<PageOccurrences>>>() {});
        List<PageOccurrences> result = index.getOrDefault(query.toLowerCase(), Collections.emptyList());
        result.sort((a, b) -> Integer.compare(b.getOccurrences(), a.getOccurrences()));
        return result;
    }
    
}