package org.appledash.saneeconomy.utils;

import java.util.*;

/**
 * Created by appledash on 7/11/16.
 * Blackjack is still best pony.
 */
public class MapUtil {
    /* Originally found on StackOverflow: http://stackoverflow.com/a/2581754/1849152 */
    public static <K, V extends Comparable<? super V>> LinkedHashMap<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list =
                new LinkedList<>(map.entrySet());

        list.sort((o1, o2) -> -((o1.getValue()).compareTo(o2.getValue())));

        LinkedHashMap<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * "Skip" the given number of items in a LinkedHashMap and return a new LinkedHashMap with the remaining items.
     * @param map Map
     * @param nSkip Number of items to skip
     * @return New LinkedHashMap, may be empty.
     */
    public static <K, V> LinkedHashMap<K, V> skip(LinkedHashMap<K, V> map, int nSkip) {
        LinkedHashMap<K, V> newMap = new LinkedHashMap<>();

        if (map.size() <= nSkip) {
            return newMap;
        }

        int i = 0;

        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (i >= nSkip) {
                newMap.put(entry.getKey(), entry.getValue());
            }

            i++;
        }

        return newMap;
    }

    public static <K, V> LinkedHashMap<K, V> take(LinkedHashMap<K, V> map, int nTake) {
        LinkedHashMap<K, V> newMap = new LinkedHashMap<>();

        if (map.size() <= nTake) {
            return map;
        }

        int i = 0;

        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (i >= nTake) {
                break;
            }

            newMap.put(entry.getKey(), entry.getValue());

            i++;
        }

        return newMap;
    }

    public static <K, V> LinkedHashMap<K, V> skipAndTake(LinkedHashMap<K, V> map, int nSkip, int nTake) {
        return take(skip(map, nSkip), nTake);
    }
}
