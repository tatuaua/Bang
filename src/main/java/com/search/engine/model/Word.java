package com.search.engine.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class Word {

    String word;
    List<PageOccurrences> pageOccurrences = new ArrayList<>();

    public void addPageOccurrence(String documentName) {
        pageOccurrences.add(new PageOccurrences(documentName, 1));
    }
}
