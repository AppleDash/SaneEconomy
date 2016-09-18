package org.appledash.saneeconomy.test.mock;

import org.appledash.saneeconomy.ISaneEconomy;
import org.appledash.saneeconomy.economy.EconomyManager;
import org.appledash.saneeconomy.economy.logger.TransactionLogger;

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
    public boolean shouldLogTransactions() {
        return false;
    }

    @Override
    public TransactionLogger getTransactionLogger() {
        return null;
    }
}
