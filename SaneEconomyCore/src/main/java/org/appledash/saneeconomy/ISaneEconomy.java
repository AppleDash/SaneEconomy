package org.appledash.saneeconomy;

import org.appledash.saneeconomy.economy.EconomyManager;
import org.appledash.saneeconomy.economy.logger.TransactionLogger;

/**
 * Created by appledash on 9/18/16.
 * Blackjack is best pony.
 */
public interface ISaneEconomy {
    /**
     * Get the active EconomyManager.
     * @return EconomyManager
     */
    EconomyManager getEconomyManager();

    /**
     * Check whether transactions should be logged.
     * @return True if transactions should be logged, false otherwise.
     */
    boolean shouldLogTransactions();

    /**
     * Get the active TransactionLogger.
     * @return TransactionLogger, if there is one.
     */
    TransactionLogger getTransactionLogger();
}
