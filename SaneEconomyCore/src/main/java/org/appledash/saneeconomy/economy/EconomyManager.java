package org.appledash.saneeconomy.economy;

import org.appledash.saneeconomy.economy.backend.EconomyStorageBackend;
import org.appledash.saneeconomy.economy.economable.Economable;
import org.appledash.saneeconomy.utils.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by AppleDash on 6/13/2016.
 * Blackjack is still best pony.
 *
 * Represents our EconomyManager, which manages players' balances.
 */
public class EconomyManager {
    private final Currency currency;
    private final EconomyStorageBackend backend;

    public EconomyManager(Currency currency, EconomyStorageBackend backend) {
        this.currency = currency;
        this.backend = backend;
    }

    /**
     * Get the Currency we're using/
     * @return Currency
     */
    public Currency getCurrency() {
        return currency;
    }

    /**
     * Get the balance of a player, formatted according to our Currency's format.
     * @param player Player
     * @return Formatted balance
     */
    public String getFormattedBalance(Economable player) {
        return currency.formatAmount(backend.getBalance(player));
    }

    /**
     * Check whether a player has used the economy system before.
     * @param player Player to check
     * @return True if they have used the economy system before, false otherwise
     */
    public boolean accountExists(Economable player) {
        return backend.accountExists(player);
    }

    /**
     * Get a player's balance.
     * @param targetPlayer Player to get balance of
     * @return Player's balance
     */
    public double getBalance(Economable targetPlayer) {
        return backend.getBalance(targetPlayer);
    }


    /**
     * Check if a player has a certain amount of money.
     * @param targetPlayer Player to check balance of
     * @param requiredBalance How much money we're checking for
     * @return True if they have requiredBalance or more, false otherwise
     */
    public boolean hasBalance(Economable targetPlayer, double requiredBalance) {
        return getBalance(targetPlayer) >= requiredBalance;
    }

    /**
     * Add to a player's balance.
     * @param targetPlayer Player to add to
     * @param amount Amount to add
     * @return Player's new balance
     * @throws IllegalArgumentException If amount is negative
     */
    public double addBalance(Economable targetPlayer, double amount) {
        amount = NumberUtils.filterAmount(currency, amount);

        if (amount < 0) {
            throw new IllegalArgumentException("Cannot add a negative amount!");
        }

        double newAmount = backend.getBalance(targetPlayer) + amount;

        backend.setBalance(targetPlayer, newAmount);

        return newAmount;
    }

    /**
     * Subtract from a player's balance.
     * If the subtraction would result in a negative balance, the balance is instead set to 0.
     * @param targetPlayer Player to subtract from
     * @param amount Amount to subtract
     * @return Player's new balance
     * @throws IllegalArgumentException If amount is negative
     */
    public double subtractBalance(Economable targetPlayer, double amount) {
        amount = NumberUtils.filterAmount(currency, amount);

        if (amount < 0) {
            throw new IllegalArgumentException("Cannot subtract a negative amount!");
        }

        double newAmount = backend.getBalance(targetPlayer) - amount;


        /* Subtracting that much would result in a negative balance - don't do that */
        if (newAmount <= 0.0D) {
            newAmount = 0.0D;
        }

        backend.setBalance(targetPlayer, newAmount);

        return newAmount;
    }

    /**
     * Set a player's balance.
     * @param targetPlayer Player to set balance of
     * @param amount Amount to set balance to
     * @throws IllegalArgumentException If amount is negative
     */
    public void setBalance(Economable targetPlayer, double amount) {
        amount = NumberUtils.filterAmount(currency, amount);

        if (amount < 0) {
            throw new IllegalArgumentException("Cannot set balance to a negative value!");
        }

        backend.setBalance(targetPlayer, amount);
    }

    /**
     * Transfer money from one player to another.
     * @param fromPlayer Player to transfer from
     * @param toPlayer Player to transfer to
     * @param amount Amount to transfer
     * @return True if success, false if fromPlayer has insufficient funds.
     * @throws IllegalArgumentException If amount is negative
     */
    public boolean transfer(Economable fromPlayer, Economable toPlayer, double amount) {
        amount = NumberUtils.filterAmount(currency, amount);

        if (amount < 0) {
            throw new IllegalArgumentException("Cannot transfer a negative amount!");
        }

        if (!hasBalance(fromPlayer, amount)) {
            return false;
        }

        /* Perform the actual transfer. TODO: Maybe return their new balances in some way? */
        subtractBalance(fromPlayer, amount);
        addBalance(toPlayer, amount);

        return true;
    }

    /**
     * Get the players who have the most money.
     * @param amount Maximum number of players to show.
     * @return Map of OfflinePlayer to Double
     */
    public Map<OfflinePlayer, Double> getTopPlayerBalances(int amount) {
        Map<UUID, Double> uuidBalances = backend.getTopPlayerBalances(amount);
        Map<OfflinePlayer, Double> playerBalances = new LinkedHashMap<>();

        uuidBalances.forEach((uuid, balance) -> playerBalances.put(Bukkit.getServer().getOfflinePlayer(uuid), balance));

        return playerBalances;
    }

    public EconomyStorageBackend getBackend() {
        return backend;
    }
}
