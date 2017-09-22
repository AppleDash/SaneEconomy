package org.appledash.saneeconomy.test.mock;

import org.appledash.saneeconomy.ISaneEconomy;
import org.appledash.saneeconomy.economy.EconomyManager;
import org.appledash.saneeconomy.economy.logger.TransactionLogger;
import org.appledash.saneeconomy.vault.VaultHook;

import java.util.Optional;

/**
 * Created by appledash on 9/18/16.
 * Blackjack is best pony.
 */
public class MockSaneEconomy implements ISaneEconomy {
    @Override
    public EconomyManager getEconomyManager() {
        return null;
    }

    @Override
    public Optional<TransactionLogger> getTransactionLogger() {
        return Optional.empty();
    }

    @Override
    public VaultHook getVaultHook() {
        return null;
    }
}
