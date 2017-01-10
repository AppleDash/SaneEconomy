package org.appledash.saneeconomysignshop.util;

import java.util.HashMap;
import java.util.function.Supplier;

/**
 * Created by appledash on 1/1/17.
 * Blackjack is still best pony.
 */
public class DefaultHashMap<K, V> extends HashMap<K, V> {
    private final Supplier<V> defaultSupplier;

    public DefaultHashMap(Supplier<V> defaultSupplier) {
        if (defaultSupplier == null) {
            throw new NullPointerException("defaultSupplier is null");
        }

        this.defaultSupplier = defaultSupplier;
    }

    @Override
    public V get(Object k) {
        V v = super.get(k);

        if (v == null) {
            v = defaultSupplier.get();
            this.put((K) k, v);
        }

        return v;
    }
}
