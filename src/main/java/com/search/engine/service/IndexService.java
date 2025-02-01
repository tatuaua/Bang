package com.search.engine.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.search.engine.model.PageOccurrences;

import jakarta.annotation.PostConstruct;

import static com.search.engine.util.Constants.*;

@Service
public class IndexService {

    @PostConstruct
    public void createIndexOnStartup() throws IOException {
        System.out.println("Indexing started...");
        createIndex();
        System.out.println("Indexing completed!");
    }

    public void createIndex() throws IOException {
        Map<String, List<PageOccurrences>> index = new HashMap<>();

        File dir = new File(DIRECTORY_PATH);
        
        // for each word creates one object per page where it occurs and how many times

        for (File file : Objects.requireNonNull(dir.listFiles())) {
            String page = file.getName();
            List<String> lines = Files.readAllLines(file.toPath());
            for (String line : lines) {
                String[] words = line.split("\\W+");
                for (String word : words) {
                    String key = word.toLowerCase();
                    if (!index.containsKey(key)) {
                        index.put(key, new ArrayList<>());
                    }
                    List<PageOccurrences> occurrences = index.get(key);
                    boolean found = false;
                    for (PageOccurrences pageOccurrences : occurrences) {
                        if (pageOccurrences.getPage().equals(page)) {
                            pageOccurrences.setOccurrences(pageOccurrences.getOccurrences() + 1);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        occurrences.add(new PageOccurrences(page, 1));
                    }
                }
            }
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(new File(INDEX_FILE), index);
    }

}
