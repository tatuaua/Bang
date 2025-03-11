package com.search.bang.repository;

import com.search.bang.model.PageOccurrences;
import com.search.bang.model.Word;
import java.util.List;

public interface DatabaseRepository {
    void init();
    void upsertIndex(List<Word> index);
    List<PageOccurrences> getTop5DocumentsFuzzy(String word);
    List<PageOccurrences> getTop5DocumentsExact(String word);
}