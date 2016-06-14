package org.appledash.saneeconomy.economy.backend;

import org.bukkit.entity.Player;

/**
 * Created by AppleDash on 6/13/2016.
 * Blackjack is still best pony.
 *
 * Represents our economy storage backend - whatever we're using to store economy data.
 */
public interface EconomyStorageBackend {
    /**
     * Get the balance of a player.
     * @param player Player
     * @return Player's current balance
     */
    double getBalance(Player player);

    /**
     * Set the balance of a player, overwriting the old balance.
     * @param player Player
     * @param newBalance Player's new balance
     */
    void setBalance(Player player, double newBalance);

    /**
     * Add to a player's balance.
     * @param player Player
     * @param amount Amount to add to the balance
     * @return Player's new balance
     */
    double addBalance(Player player, double amount);

    /**
     * Subtract from a player's balance.
     * @param player Player
     * @param amount Amount to subtract from the balance
     * @return Player's new balance
     */
    double subtractBalance(Player player, double amount);
}
