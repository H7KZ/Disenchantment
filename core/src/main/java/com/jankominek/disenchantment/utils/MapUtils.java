package com.jankominek.disenchantment.utils;

import java.util.*;

public class MapUtils {
    public static <T> HashMap<T, Integer> sortByValue(HashMap<T, Integer> hashMap) {
        return MapUtils.sortByValue(hashMap, false);
    }

    public static <T> HashMap<T, Integer> sortByValue(HashMap<T, Integer> hashMap, boolean reverse) {
        List<Map.Entry<T, Integer>> list = new LinkedList<>(hashMap.entrySet());

        if (reverse) {
            list.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        } else {
            list.sort(Map.Entry.comparingByValue());
        }

        HashMap<T, Integer> temp = new LinkedHashMap<>();

        for (Map.Entry<T, Integer> entry : list) {
            temp.put(entry.getKey(), entry.getValue());
        }

        return temp;
    }

    public static <T> Map<T, Integer> sortByValue(Map<T, Integer> hashMap) {
        return MapUtils.sortByValue(hashMap, false);
    }

    public static <T> Map<T, Integer> sortByValue(Map<T, Integer> hashMap, boolean reverse) {
        List<Map.Entry<T, Integer>> list = new LinkedList<>(hashMap.entrySet());

        if (reverse) {
            list.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        } else {
            list.sort(Map.Entry.comparingByValue());
        }

        Map<T, Integer> temp = new LinkedHashMap<>();

        for (Map.Entry<T, Integer> entry : list) {
            temp.put(entry.getKey(), entry.getValue());
        }

        return temp;
    }
}
