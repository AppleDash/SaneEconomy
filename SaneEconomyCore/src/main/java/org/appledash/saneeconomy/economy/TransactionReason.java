package org.appledash.saneeconomy.economy;

/**
 * Created by AppleDash on 8/15/2016.
 * Blackjack is still best pony.
 */
public enum TransactionReason {
    /**
     * A player paying another player.
     */
    PLAYER_PAY,
    /**
     * An admin giving a player money.
     */
    ADMIN,
    /**
     * Another plugin using the API.
     */
    PLUGIN
}
