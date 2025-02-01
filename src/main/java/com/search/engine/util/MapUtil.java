package com.search.engine.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MapUtil {
    

    public static Map<String, List<Integer>> sort(Map<String, List<Integer>> innerMap) {

        List<Map.Entry<String, List<Integer>>> sortedEntries = new ArrayList<>(innerMap.entrySet());

        Comparator<Map.Entry<String, List<Integer>>> comparator = Comparator.comparingInt(entry -> entry.getValue().size());
        
        sortedEntries.sort(comparator.reversed());
        
        Map<String, List<Integer>> sortedInnerMap = new LinkedHashMap<>();
        for (Map.Entry<String, List<Integer>> entry : sortedEntries) {
            sortedInnerMap.put(entry.getKey(), entry.getValue());
        }

        return sortedInnerMap;
    }

}
