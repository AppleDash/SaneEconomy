package org.appledash.saneeconomy.test.mock;

import org.appledash.saneeconomy.economy.backend.type.EconomyStorageBackendCaching;
import org.appledash.saneeconomy.economy.economable.Economable;

import java.math.BigDecimal;

/**
 * Created by AppleDash on 7/29/2016.
 * Blackjack is still best pony.
 */
public class MockEconomyStorageBackend extends EconomyStorageBackendCaching {
    @Override
    public void setBalance(Economable player, BigDecimal newBalance) {
        this.balances.put(player.getUniqueIdentifier(), newBalance);
        this.uuidToName.put(player.getUniqueIdentifier(), player.getName());
    }

    @Override
    public void reloadDatabase() {
        System.out.println("Reloading mock economy database (doing nothing).");
    }

    @Override
    public void waitUntilFlushed() {
        // Null op
    }
}
