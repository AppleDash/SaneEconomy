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
    @SuppressWarnings("unchecked")
    public V get(Object key) {
        V value = super.get(key);

        if (value == null) {
            value = this.defaultSupplier.get((K)key);
            this.put((K) key, value);
        }

        return value;
    }

    public interface KeyBasedSupplier<K, V> {
        V get(K k);
    }
}
