package org.appledash.saneeconomy.economy.transaction;

import org.appledash.saneeconomy.economy.economable.Economable;
import org.appledash.saneeconomy.economy.transaction.TransactionReason.AffectedParties;

/**
 * Created by appledash on 9/21/16.
 * Blackjack is best pony.
 */
public class Transaction {
    private final Economable sender;
    private final Economable receiver;
    private final double amount;
    private final TransactionReason reason;

    public Transaction(Economable sender, Economable receiver, double amount, TransactionReason reason) {
        if (amount <= 0.0) {
            throw new IllegalArgumentException("Cannot transact a zero or negative amount!");
        }

        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
        this.reason = reason;
    }

    public Economable getSender() {
        return sender;
    }

    public Economable getReceiver() {
        return receiver;
    }

    public double getAmount() {
        return amount;
    }

    public TransactionReason getReason() {
        return reason;
    }

    public boolean isSenderAffected() {
        if (sender == Economable.CONSOLE) {
            return false;
        }

        return (reason.getAffectedParties() == AffectedParties.SENDER) || (reason.getAffectedParties() == AffectedParties.BOTH);
    }

    public boolean isReceiverAffected() {
        if (receiver == Economable.CONSOLE) {
            return false;
        }

        return (reason.getAffectedParties() == AffectedParties.RECEIVER) || (reason.getAffectedParties() == AffectedParties.BOTH);
    }
}
