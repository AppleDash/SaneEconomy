package org.appledash.saneeconomy.economy.backend.type;

import org.appledash.saneeconomy.economy.backend.EconomyStorageBackend;
import org.appledash.saneeconomy.utils.MapUtil;
import org.bukkit.OfflinePlayer;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by appledash on 7/19/16.
 * Blackjack is still best pony.
 */
public abstract class EconomyStorageBackendCaching implements EconomyStorageBackend {
    protected HashMap<UUID, Double> playerBalances = new HashMap<>();
    protected Map<UUID, Double> topBalances = new LinkedHashMap<>();

    @Override
    public boolean accountExists(OfflinePlayer player) {
        return playerBalances.containsKey(player.getUniqueId());
    }

    @Override
    public synchronized double getBalance(OfflinePlayer player) {
        if (!accountExists(player)) {
            return 0.0D;
        }

        return playerBalances.get(player.getUniqueId());
    }

    @Override
    public Map<UUID, Double> getTopBalances(int amount) {
        return MapUtil.takeFromMap(topBalances, amount);
    }

    @Override
    public void reloadTopBalances() {
        topBalances = MapUtil.sortByValue(playerBalances);
    }
}
