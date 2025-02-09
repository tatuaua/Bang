package com.search.engine.repository;

import com.search.engine.model.PageOccurrences;
import com.search.engine.model.Word;
import java.util.List;

public interface DatabaseRepository {
    
    void init();
    void upsertIndex(List<Word> index);
    List<PageOccurrences> getTop5Documents(String word, boolean isOriginalWord);
}