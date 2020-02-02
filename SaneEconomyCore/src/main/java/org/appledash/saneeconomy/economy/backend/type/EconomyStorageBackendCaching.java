package org.appledash.saneeconomy.economy.backend.type;

import com.google.common.collect.ImmutableMap;
import org.appledash.saneeconomy.SaneEconomy;
import org.appledash.saneeconomy.economy.backend.EconomyStorageBackend;
import org.appledash.saneeconomy.economy.economable.Economable;
import org.appledash.saneeconomy.utils.MapUtil;

import java.math.BigDecimal;
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
    protected Map<String, BigDecimal> balances = new ConcurrentHashMap<>();
    private LinkedHashMap<String, BigDecimal> topBalances = new LinkedHashMap<>();
    protected Map<String, String> uuidToName = new HashMap<>();

    @Override
    public boolean accountExists(Economable economable) {
        return this.balances.containsKey(economable.getUniqueIdentifier());
    }

    @Override
    public BigDecimal getBalance(Economable economable) {
        if (!this.accountExists(economable)) {
            return BigDecimal.ZERO;
        }

        return this.balances.get(economable.getUniqueIdentifier());
    }

    public LinkedHashMap<String, BigDecimal> getTopBalances() {
        return this.topBalances;
    }

    @Override
    public void reloadTopPlayerBalances() {
        Map<String, BigDecimal> balances = new HashMap<>();

        this.balances.forEach((identifier, balance) -> {
            balances.put(this.uuidToName.get(identifier), balance);
        });

        this.topBalances = MapUtil.sortByValue(balances);
    }

    @Override
    public Map<String, BigDecimal> getAllBalances() {
        return ImmutableMap.copyOf(this.balances);
    }

    @Override
    public void reloadEconomable(String uniqueIdentifier, EconomableReloadReason reason) {
        if (reason == EconomableReloadReason.CROSS_SERVER_SYNC) {
            SaneEconomy.logger().warning("Trying to reload a single Economable from backend which does not support this - " + this.getClass().getSimpleName() + ". Recommend switching to MySQL backend for multi-server support.");
        }

        this.reloadDatabase();
    }

    @Override
    public String getLastName(String uuid) {
        return this.uuidToName.get(uuid);
    }
}
