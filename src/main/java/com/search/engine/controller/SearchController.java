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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.search.engine.util.MapUtil;

import static com.search.engine.util.Constants.*;

@RestController
@RequestMapping("/search")
public class SearchController {

    @GetMapping("/{query}")
    public Map<String, List<Integer>> search(@PathVariable String query) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        @SuppressWarnings("unchecked")
        Map<String, Map<String, List<Integer>>> index = mapper.readValue(new File(INDEX_FILE), Map.class);
        return MapUtil.sort(index.getOrDefault(query.toLowerCase(), Collections.emptyMap()));
    }
}