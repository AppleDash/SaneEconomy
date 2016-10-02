package org.appledash.saneeconomy.economy.transaction;

/**
 * Created by AppleDash on 8/15/2016.
 * Blackjack is still best pony.
 */
public enum TransactionReason {
    /**
     * A player paying another player.
     */
    PLAYER_PAY(AffectedParties.BOTH),
    /**
     * An admin giving a player money.
     */
    ADMIN_GIVE(AffectedParties.RECEIVER),
    ADMIN_TAKE(AffectedParties.SENDER),
    /**
     * Another plugin using the API.
     */
    PLUGIN_GIVE(AffectedParties.RECEIVER),
    PLUGIN_TAKE(AffectedParties.SENDER),
    /**
     * Initial starting balance on join.
     */
    STARTING_BALANCE(AffectedParties.RECEIVER),
    /**
     * Used in unit tests.
     */
    TEST_GIVE(AffectedParties.RECEIVER),
    TEST_TAKE(AffectedParties.SENDER);

    private final AffectedParties affectedParties;

    TransactionReason(AffectedParties affectedParties) {
        this.affectedParties = affectedParties;
    }

    public AffectedParties getAffectedParties() {
        return affectedParties;
    }

    public enum AffectedParties {
        SENDER,
        RECEIVER,
        BOTH
    }
}
