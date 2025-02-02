package com.search.engine.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.search.engine.model.PageOccurrences;
import com.search.engine.util.Database;

import jakarta.annotation.PostConstruct;

import static com.search.engine.util.Constants.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class IndexService {

    private final AtomicInteger indexedWords = new AtomicInteger(0);
    private final AtomicInteger totalWords = new AtomicInteger(0);

    @PostConstruct
    public void createIndexOnStartup() throws IOException {
        System.out.println("Indexing started...");
        startTimer();
        createIndex();
        System.out.println("Indexing completed!");
    }

    private void startTimer() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                if(totalWords.get() == indexedWords.get()) {
                    return;
                }

                System.out.println("Indexed words: " + indexedWords.get() + ", Total words: " + totalWords.get());
            }
        }, 0, 5000);
    }

    public void createIndex() throws IOException {

        Database.init();

        File directory = new File(DIRECTORY_PATH);
        File[] files = directory.listFiles();
        if (files == null) {
            throw new RuntimeException("No files found in the directory");
        }

        for (File file : files) {

            System.out.println("Indexing file: " + file.getName());
            String documentName = file.getName();
            String content = Files.readString(file.toPath());
            String[] words = content.split("\\s+");
            totalWords.addAndGet(words.length);

            for (String word : words) {

                if (STOP_WORDS.contains(word)) {
                    indexedWords.incrementAndGet();
                    continue;
                }
                
                Database.insertWordIfAbsent(word);

                PageOccurrences pageOccurrences = Database.getPageOccurrences(word, documentName);

                if (Objects.isNull(pageOccurrences)) {
                    Database.insertPageOccurrences(word, documentName, 1);
                } else {
                    Database.updatePageOccurrences(word, documentName, pageOccurrences.getOccurrences() + 1);
                }

                indexedWords.incrementAndGet();
            }
        }

        Database.closeConnection();
    }

}
