package org.appledash.saneeconomy.economy.logger;

import org.appledash.saneeconomy.economy.TransactionReason;
import org.appledash.saneeconomy.economy.economable.Economable;

/**
 * Created by AppleDash on 8/15/2016.
 * Blackjack is still best pony.
 */
public interface TransactionLogger {
    void logAddition(Economable economable, double amount, TransactionReason reason);
    void logSubtraction(Economable economable, double amount, TransactionReason reason);
    void logTransfer(Economable from, Economable to, double amount, TransactionReason reason);
}
