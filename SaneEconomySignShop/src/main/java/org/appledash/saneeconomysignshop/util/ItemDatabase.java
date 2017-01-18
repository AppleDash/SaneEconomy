package org.appledash.saneeconomysignshop.util;

import com.google.common.collect.ImmutableMap;
import org.bukkit.Material;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by appledash on 8/3/16.
 * Blackjack is still best pony.
 */
public class ItemDatabase {
    private static Map<String, Pair<Integer, Short>> itemMap = new HashMap<>();

    public static void initItemDB() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(ItemDatabase.class.getResourceAsStream("/items.csv")))) {
            String line;

            while ((line = br.readLine()) != null) {
                if (line.startsWith("#") || !line.contains(",")) {
                    continue;
                }

                String[] split = line.split(",");
                String name = split[0];
                int id = Integer.valueOf(split[1]);
                short damage = Short.valueOf(split[2]);

                itemMap.put(name.toLowerCase(), Pair.of(id, damage));
            }

            itemMap = ImmutableMap.copyOf(itemMap);
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    public static Optional<Pair<Integer, Short>> getIDAndDamageForName(String name) {
        if (Material.getMaterial(name) != null) {
            return Optional.of(Pair.of(Material.getMaterial(name).getId(), (short) 0));
        }
        return Optional.ofNullable(itemMap.get(name.toLowerCase()));
    }

    public static class Pair<K, V> {
        private K k;
        private V v;
        public Pair(K k, V v) {
            this.k = k;
            this.v = v;
        }

        public K getLeft() {
            return k;
        }

        public V getRight() {
            return v;
        }

        public static <K, V> Pair of(K k, V v) {
            return new Pair<>(k, v);
        }
    }
}
