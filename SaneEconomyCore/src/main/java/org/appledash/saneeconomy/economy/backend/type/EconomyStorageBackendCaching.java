package org.appledash.saneeconomy.economy.backend.type;

import com.google.common.collect.ImmutableMap;
import org.appledash.saneeconomy.SaneEconomy;
import org.appledash.saneeconomy.economy.backend.EconomyStorageBackend;
import org.appledash.saneeconomy.economy.economable.Economable;
import org.appledash.saneeconomy.utils.MapUtil;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by appledash on 7/19/16.
 * Blackjack is still best pony.
 */
public abstract class EconomyStorageBackendCaching implements EconomyStorageBackend {
    protected Map<String, Double> balances = new ConcurrentHashMap<>();
    private LinkedHashMap<String, Double> topBalances = new LinkedHashMap<>();
    protected Map<String, String> uuidToName = new HashMap<>();

    @Override
    public boolean accountExists(Economable economable) {
        return balances.containsKey(economable.getUniqueIdentifier());
    }

    @Override
    public double getBalance(Economable economable) {
        if (!accountExists(economable)) {
            return 0.0D;
        }

        return balances.get(economable.getUniqueIdentifier());
    }

    public LinkedHashMap<String, Double> getTopBalances() {
        return topBalances;
    }

    @Override
    public void reloadTopPlayerBalances() {
        Map<String, Double> balances = new HashMap<>();

        this.balances.forEach((identifier, balance) -> {
            balances.put(this.uuidToName.get(identifier), balance);
        });

        topBalances = MapUtil.sortByValue(balances);
    }

    @Override
    public Map<String, Double> getAllBalances() {
        return ImmutableMap.copyOf(balances);
    }

    @Override
    public void reloadEconomable(String uniqueIdentifier, EconomableReloadReason reason) {
        if (reason == EconomableReloadReason.CROSS_SERVER_SYNC) {
            SaneEconomy.logger().warning("Trying to reload a single Economable from backend which does not support this - " + this.getClass().getSimpleName() + ". Recommend switching to MySQL backend for multi-server support.");
        }

        this.reloadDatabase();
    }
}
