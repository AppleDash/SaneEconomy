package org.appledash.saneeconomy.event;

import org.appledash.saneeconomy.economy.transaction.Transaction;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created by appledash on 7/5/17.
 * Blackjack is best pony.
 *
 * This event is called whenever a Transaction occurs in the plugin. If you cancel this event, the transaction will be cancelled as well.
 */
public class SaneEconomyTransactionEvent extends Event implements Cancellable {
    private static final HandlerList handlerList = new HandlerList();
    private final Transaction transaction;
    private boolean isCancelled;

    public SaneEconomyTransactionEvent(Transaction transaction) {
        //super(!Bukkit.getServer().isPrimaryThread());
        this.transaction = transaction;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.isCancelled = cancelled;
    }

    public Transaction getTransaction() {
        return this.transaction;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
