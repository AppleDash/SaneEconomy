package org.appledash.saneeconomy.economy.transaction;

import org.appledash.saneeconomy.economy.Currency;
import org.appledash.saneeconomy.economy.economable.Economable;
import org.appledash.saneeconomy.economy.transaction.TransactionReason.AffectedParties;
import org.appledash.saneeconomy.utils.NumberUtils;

import java.math.BigDecimal;

/**
 * Created by appledash on 9/21/16.
 * Blackjack is best pony.
 */
public class Transaction {
    private final Economable sender;
    private final Economable receiver;
    private final BigDecimal amount;
    private final TransactionReason reason;

    public Transaction(Currency currency, Economable sender, Economable receiver, BigDecimal amount, TransactionReason reason) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Cannot transact a zero or negative amount!");
        }


        this.sender = sender;
        this.receiver = receiver;
        this.amount = NumberUtils.filterAmount(currency, amount);
        this.reason = reason;
    }

    public Economable getSender() {
        return this.sender;
    }

    public Economable getReceiver() {
        return this.receiver;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public TransactionReason getReason() {
        return this.reason;
    }

    public boolean isSenderAffected() {
        if (this.sender == Economable.CONSOLE) {
            return false;
        }

        return (this.reason.getAffectedParties() == AffectedParties.SENDER) || (this.reason.getAffectedParties() == AffectedParties.BOTH);
    }

    public boolean isReceiverAffected() {
        if (this.receiver == Economable.CONSOLE) {
            return false;
        }

        return (this.reason.getAffectedParties() == AffectedParties.RECEIVER) || (this.reason.getAffectedParties() == AffectedParties.BOTH);
    }
}
