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
        Map<String, Map<String, List<Integer>>> index = new HashMap<>();

        File dir = new File(DIRECTORY_PATH);
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            String content = Files.readString(file.toPath());
            String[] words = content.split("\\W+"); // Split on non-word characters

            for (int i = 0; i < words.length; i++) {
                String word = words[i].toLowerCase();
                index.putIfAbsent(word, new HashMap<>());
                index.get(word).putIfAbsent(file.getName(), new ArrayList<>());
                index.get(word).get(file.getName()).add(i);
            }
        }

        // Save index as JSON
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(new File(INDEX_FILE), index);
    }

}
