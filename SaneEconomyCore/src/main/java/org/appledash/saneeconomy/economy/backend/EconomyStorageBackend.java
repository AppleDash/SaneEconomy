package org.appledash.saneeconomy.economy.backend;

import org.appledash.saneeconomy.economy.economable.Economable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by AppleDash on 6/13/2016.
 * Blackjack is still best pony.
 *
 * Represents our economy storage backend - whatever we're using to store economy data.
 */
public interface EconomyStorageBackend {
    /**
     * Check whether an economable has used the economy system before.
     * @param economable Economable
     * @return True if they have, false otherwise.
     */
    boolean accountExists(Economable economable);

    /**
     * Get the balance of a player.
     * @param economable Economable
     * @return Player's current balance
     */
    double getBalance(Economable economable);

    /**
     * Set the balance of an Economable, overwriting the old balance.
     * @param economable Economable
     * @param newBalance Player's new balance
     */
    void setBalance(Economable economable, double newBalance);

    /**
     * Get the UUIDs of the players who have the most money, along with how much money they have.
     * @return Map of player UUIDs to amounts.
     */
    LinkedHashMap<UUID, Double> getTopPlayerBalances();

    /**
     * Reload this backend's database from disk.
     */
    void reloadDatabase();

    /**
     * Reload data for just the Economable with the given unique identifier.
     * @param uniqueIdentifier Unique identifier of Economable to reload data for.
     */
    void reloadEconomable(String uniqueIdentifier);

    /**
     * Reload this backend's top balances.
     */
    void reloadTopPlayerBalances();

    /**
     * Get the balances of all entities in this database.
     * @return Map of unique identifiers to balances.
     */
    Map<String, Double> getAllBalances();

    /**
     * Wait until all of the data in memory has been written out to disk.
     */
    void waitUntilFlushed();
}
