package org.appledash.saneeconomy.economy.transaction;

/**
 * Created by appledash on 9/21/16.
 * Blackjack is best pony.
 */
public class TransactionResult {
    private final Transaction transaction;
    private final double fromBalance;
    private final double toBalance;
    private Status status;

    public TransactionResult(Transaction transaction, double fromBalance, double toBalance) {
        this.transaction = transaction;
        this.fromBalance = fromBalance;
        this.toBalance = toBalance;
        this.status = Status.SUCCESS;
    }

    public TransactionResult(Transaction transaction, Status status) {
        this(transaction, -1, -1);
        this.status = status;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public double getFromBalance() {
        return fromBalance;
    }

    public double getToBalance() {
        return toBalance;
    }

    public Status getStatus() {
        return status;
    }

    public enum Status {
        SUCCESS("Success."),
        CANCELLED_BY_PLUGIN("That transaction was cancelled by a plugin."), // In theory this message should never be shown.
        ERR_NOT_ENOUGH_FUNDS("Not enough money is available for you to complete that transaction.");

        private final String message;

        Status(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
