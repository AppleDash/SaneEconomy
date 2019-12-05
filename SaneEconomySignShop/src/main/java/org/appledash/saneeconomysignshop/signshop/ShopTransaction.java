package org.appledash.saneeconomysignshop.signshop;

import org.appledash.saneeconomy.economy.Currency;
import org.appledash.saneeconomy.economy.economable.Economable;
import org.appledash.saneeconomy.economy.transaction.Transaction;
import org.appledash.saneeconomy.economy.transaction.TransactionReason;
import org.appledash.saneeconomysignshop.util.ItemInfo;
import org.bukkit.entity.Player;

/**
 * Created by appledash on 1/1/17.
 * Blackjack is still best pony.
 */
public class ShopTransaction {
    private final Currency currency;
    // Direction is always what the player is doing. BUY = player is buying from shop.
    private final TransactionDirection direction;
    private final Player player;
    private final ItemInfo item;
    private final int quantity;
    private final double price;

    public ShopTransaction(Currency currency, TransactionDirection direction, Player player, ItemInfo item, int quantity, double price) {
        this.currency = currency;
        this.direction = direction;
        this.player = player;
        this.item = item;
        this.quantity = quantity;
        this.price = price;
    }

    public TransactionDirection getDirection() {
        return this.direction;
    }

    public Player getPlayer() {
        return this.player;
    }

    public ItemInfo getItem() {
        return this.item;
    }

    public int getQuantity() {
        return this.quantity;
    }

    public double getPrice() {
        return this.price;
    }

    public Transaction makeEconomyTransaction() {
        if (this.direction == TransactionDirection.BUY) {
            return new Transaction(this.currency, Economable.wrap(this.player), Economable.PLUGIN, this.price, TransactionReason.PLUGIN_TAKE);
        } else {
            return new Transaction(this.currency, Economable.PLUGIN, Economable.wrap(this.player), this.price, TransactionReason.PLUGIN_GIVE);
        }
    }

    public enum TransactionDirection {
        BUY, SELL
    }
}
