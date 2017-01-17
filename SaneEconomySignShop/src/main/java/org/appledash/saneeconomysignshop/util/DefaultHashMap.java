package org.appledash.saneeconomysignshop.util;

import java.util.HashMap;
import java.util.function.Supplier;

/**
 * Created by appledash on 1/1/17.
 * Blackjack is still best pony.
 */
public class DefaultHashMap<K, V> extends HashMap<K, V> {
    private final KeyBasedSupplier<K, V> defaultSupplier;

    public DefaultHashMap(Supplier<V> defaultSupplier) {
        this((k) -> defaultSupplier.get());
    }

    public DefaultHashMap(KeyBasedSupplier<K, V> supplier) {
        this.defaultSupplier = supplier;
    }

    @Override
    public V get(Object k) {
        V v = super.get(k);

        if (v == null) {
            v = defaultSupplier.get((K)k);
            this.put((K) k, v);
        }

        return v;
    }

    public interface KeyBasedSupplier<K, V> {
        V get(K k);
    }
}
