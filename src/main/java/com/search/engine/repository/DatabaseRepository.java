package com.search.engine.repository;

import com.search.engine.model.PageOccurrences;
import com.search.engine.model.Word;
import java.util.List;

public interface DatabaseRepository {
    void open();
    void close();
    void init();
    
    void upsertIndex(List<Word> index);
    
    void insertWordIfAbsent(String word);
    void insertPageOccurrencesIfAbsent(String word, String documentName, int occurrences);
    
    PageOccurrences getPageOccurrences(String word, String documentName);
    List<PageOccurrences> getTop5Documents(String word);
}