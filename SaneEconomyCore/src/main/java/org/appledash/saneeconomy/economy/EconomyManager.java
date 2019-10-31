package org.appledash.saneeconomy.economy;

import org.appledash.saneeconomy.ISaneEconomy;
import org.appledash.saneeconomy.SaneEconomy;
import org.appledash.saneeconomy.economy.backend.EconomyStorageBackend;
import org.appledash.saneeconomy.economy.economable.Economable;
import org.appledash.saneeconomy.economy.transaction.Transaction;
import org.appledash.saneeconomy.economy.transaction.TransactionResult;
import org.appledash.saneeconomy.event.SaneEconomyTransactionEvent;
import org.appledash.saneeconomy.utils.MapUtil;
import org.appledash.saneeconomy.utils.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by AppleDash on 6/13/2016.
 * Blackjack is still best pony.
 *
 * Represents our EconomyManager, which manages players' balances.
 */
public class EconomyManager {
    private final ISaneEconomy saneEconomy;
    private final Currency currency;
    private final EconomyStorageBackend backend;
    private final String serverAccountName;

    public EconomyManager(ISaneEconomy saneEconomy, Currency currency, EconomyStorageBackend backend, String serverAccountName) {
        this.saneEconomy = saneEconomy;
        this.currency = currency;
        this.backend = backend;
        this.serverAccountName = serverAccountName;
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
        if (targetPlayer == Economable.CONSOLE) {
            return Double.MAX_VALUE;
        }

        return backend.getBalance(targetPlayer);
    }


    /**
     * Check if a player has a certain amount of money.
     * @param targetPlayer Player to check balance of
     * @param requiredBalance How much money we're checking for
     * @return True if they have requiredBalance or more, false otherwise
     */
    public boolean hasBalance(Economable targetPlayer, double requiredBalance) {
        return (targetPlayer == Economable.CONSOLE) || (getBalance(targetPlayer) >= requiredBalance);

    }

    /**
     * Add to a player's balance.
     * This does not filter the amount.
     * @param targetPlayer Player to add to
     * @param amount Amount to add
     * @throws IllegalArgumentException If amount is negative
     */
    private void addBalance(Economable targetPlayer, double amount) {
        setBalance(targetPlayer, backend.getBalance(targetPlayer) + amount);
    }

    /**
     * Subtract from a player's balance.
     * If the subtraction would result in a negative balance, the balance is instead set to 0.
     * This does not filter the amount.
     *
     * @param targetPlayer Player to subtract from
     * @param amount Amount to subtract
     * @throws IllegalArgumentException If amount is negative
     */
    private void subtractBalance(Economable targetPlayer, double amount) {
        // Ensure we don't go negative.
        setBalance(targetPlayer, Math.max(0.0, backend.getBalance(targetPlayer) - amount));
    }

    /**
     * Set a player's balance. This does NOT log.
     * @param targetPlayer Player to set balance of
     * @param amount Amount to set balance to
     * @throws IllegalArgumentException If amount is negative
     */
    public void setBalance(Economable targetPlayer, double amount) {
        amount = NumberUtils.filterAmount(currency, amount);

        if (targetPlayer == Economable.CONSOLE) {
            return;
        }

        backend.setBalance(targetPlayer, amount);
    }

    /**
     * Perform a transaction - a transfer of funds from one entity to another.
     * @param transaction Transaction to perform.
     * @return TransactionResult describing success or failure of the Transaction.
     */
    public TransactionResult transact(Transaction transaction) {
        Economable sender = transaction.getSender();
        Economable receiver = transaction.getReceiver();
        double amount = transaction.getAmount(); // This amount is validated and filtered upon creation of Transaction

        if (Bukkit.getServer().getPluginManager() != null) { // Bukkit.getServer().getPluginManager() == null from our JUnit tests.
            SaneEconomyTransactionEvent evt = new SaneEconomyTransactionEvent(transaction);

            Future<SaneEconomyTransactionEvent> future = Bukkit.getServer().getScheduler().callSyncMethod(SaneEconomy.getInstance(), () -> {
                Bukkit.getServer().getPluginManager().callEvent(evt);
                return evt;
            });

            try {
                if (future.get().isCancelled()) {
                    return new TransactionResult(transaction, TransactionResult.Status.CANCELLED_BY_PLUGIN);
                }
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }

            /*
            Bukkit.getServer().getPluginManager().callEvent(evt);
            if (evt.isCancelled()) {
                return new TransactionResult(transaction, TransactionResult.Status.CANCELLED_BY_PLUGIN);
            }*/
        }

        if (transaction.isSenderAffected()) { // Sender should have balance taken from them
            if (!hasBalance(sender, amount)) {
                return new TransactionResult(transaction, TransactionResult.Status.ERR_NOT_ENOUGH_FUNDS);
            }

            subtractBalance(sender, amount);
        }

        if (transaction.isReceiverAffected()) { // Receiver should have balance added to them
            addBalance(receiver, amount);
        }

        saneEconomy.getTransactionLogger().ifPresent((logger) -> logger.logTransaction(transaction));

        return new TransactionResult(transaction, getBalance(sender), getBalance(receiver));
    }

    /**
     * Get the players who have the most money.
     * @param amount Maximum number of players to show.
     * @return Map of OfflinePlayer to Double
     */
    public Map<String, Double> getTopBalances(int amount, int offset) {
        LinkedHashMap<String, Double> uuidBalances = backend.getTopBalances();

        /* TODO
        uuidBalances.forEach((uuid, balance) -> {
            OfflinePlayer offlinePlayer = Bukkit.getServer().getOfflinePlayer(uuid);
            if (offlinePlayer != null) {
                if ((this.saneEconomy.getVaultHook() == null) || !this.saneEconomy.getVaultHook().hasPermission(offlinePlayer, "saneeconomy.balancetop.hide")) {
                    playerBalances.put(Bukkit.getServer().getOfflinePlayer(uuid), balance);
                }
            }
        });
        */

        return MapUtil.skipAndTake(uuidBalances, offset, amount);
    }

    public EconomyStorageBackend getBackend() {
        return backend;
    }

    /**
     * Get the name of the "Server" economy account. This account has an infinite balance and deposits do nothing.
     * @return Server economy account, or null if none.
     */
    public String getServerAccountName() {
        return serverAccountName;
    }
}
