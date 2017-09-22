package org.appledash.saneeconomy.economy.logger;

import org.appledash.saneeconomy.economy.transaction.Transaction;

/**
 * Created by AppleDash on 8/15/2016.
 * Blackjack is still best pony.
 */
public interface TransactionLogger {
    void logTransaction(Transaction transaction);
}
