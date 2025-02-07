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

    public void updatePageOccurrence(String documentName) {
        PageOccurrences pageOccurrence = pageOccurrences.stream()
                .filter(p -> p.getPage().equals(documentName))
                .findFirst()
                .orElse(null);

        if (pageOccurrence == null) {
            pageOccurrences.add(new PageOccurrences(documentName, 1));
        } else {
            pageOccurrence.setOccurrences(pageOccurrence.getOccurrences() + 1);
        }
    }
}
