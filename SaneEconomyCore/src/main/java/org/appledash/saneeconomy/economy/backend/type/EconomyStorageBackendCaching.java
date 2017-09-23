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
    private LinkedHashMap<UUID, Double> topPlayerBalances = new LinkedHashMap<>();

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

    public LinkedHashMap<UUID, Double> getTopPlayerBalances() {
        return topPlayerBalances;
    }

    @Override
    public void reloadTopPlayerBalances() {
        Map<UUID, Double> playerBalances = new HashMap<>();

        balances.forEach((identifier, balance) -> {
            if (identifier.startsWith("player:")) { // FIXME: Come on now...
                playerBalances.put(UUID.fromString(identifier.substring("player:".length())), balance);
            }
        });

        topPlayerBalances = MapUtil.sortByValue(playerBalances);
    }

    @Override
    public Map<String, Double> getAllBalances() {
        return ImmutableMap.copyOf(balances);
    }

    @Override
    public void reloadEconomable(String uniqueIdentifier) {
        SaneEconomy.logger().warning("Trying to reload a single Economable from backend which does not support this - " + this.getClass().getSimpleName() + ". Recommend switching to MySQL backend for multi-server support.");
        this.reloadDatabase();
    }
}
