package com.jankominek.disenchantment.utils;

import java.util.*;

/**
 * Utility class for sorting maps by their integer values in ascending or descending order.
 */
public class MapUtils {
    /**
     * Sorts a {@link HashMap} by its integer values in ascending order.
     *
     * @param <T>     the key type
     * @param hashMap the map to sort
     * @return a new {@link HashMap} sorted by value in ascending order
     */
    public static <T> HashMap<T, Integer> sortByValue(HashMap<T, Integer> hashMap) {
        return MapUtils.sortByValue(hashMap, false);
    }

    /**
     * Sorts a {@link HashMap} by its integer values.
     *
     * @param <T>     the key type
     * @param hashMap the map to sort
     * @param reverse true for descending order, false for ascending
     * @return a new {@link HashMap} sorted by value
     */
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

    /**
     * Sorts a {@link Map} by its integer values in ascending order.
     *
     * @param <T>     the key type
     * @param hashMap the map to sort
     * @return a new {@link Map} sorted by value in ascending order
     */
    public static <T> Map<T, Integer> sortByValue(Map<T, Integer> hashMap) {
        return MapUtils.sortByValue(hashMap, false);
    }

    /**
     * Sorts a {@link Map} by its integer values.
     *
     * @param <T>     the key type
     * @param hashMap the map to sort
     * @param reverse true for descending order, false for ascending
     * @return a new {@link Map} sorted by value
     */
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
