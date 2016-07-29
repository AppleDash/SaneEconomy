package org.appledash.saneeconomy.test.mock;

import org.appledash.saneeconomy.economy.backend.type.EconomyStorageBackendCaching;
import org.appledash.saneeconomy.economy.economable.Economable;

/**
 * Created by AppleDash on 7/29/2016.
 * Blackjack is still best pony.
 */
public class MockEconomyStorageBackend extends EconomyStorageBackendCaching {
    @Override
    public void setBalance(Economable player, double newBalance) {
        balances.put(player.getUniqueIdentifier(), newBalance);
    }

    @Override
    public void reloadDatabase() {
        System.out.println("Reloading mock economy database (doing nothing).");
    }
}
