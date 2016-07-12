package org.appledash.saneeconomy.utils;

import java.util.*;

/**
 * Created by appledash on 7/11/16.
 * Blackjack is still best pony.
 */
public class MapUtil {
    /* Originally found on StackOverflow: http://stackoverflow.com/a/2581754/1849152 */
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list =
                new LinkedList<>(map.entrySet());
        Collections.sort(list, (o1, o2) -> (o1.getValue()).compareTo(o2.getValue()));

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public static <K, V> Map<K, V> takeFromMap(Map<K, V> map, int amount) {
        Map<K, V> newMap = new LinkedHashMap<>();

        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (newMap.size() > amount) {
                break;
            }
            newMap.put(entry.getKey(), entry.getValue());
        }

        return newMap;
    }
}
