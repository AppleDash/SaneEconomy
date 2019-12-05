package org.appledash.saneeconomysignshop.util;

/**
 * Created by appledash on 1/18/17.
 * Blackjack is best pony.
 */
public class Pair<K, V> {
    private final K left;
    private final V right;

    public Pair(K left, V right) {
        this.left = left;
        this.right = right;
    }

    public K getLeft() {
        return this.left;
    }

    public V getRight() {
        return this.right;
    }

    public static <K, V> Pair<K, V> of(K k, V v) {
        return new Pair<>(k, v);
    }
}
