package org.appledash.saneeconomysignshop.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.Serializable;

/**
 * Created by appledash on 11/3/16.
 * Blackjack is still best pony.
 */
public class ItemInfo implements Serializable {
    private Material id;
    private short damage;
    private int amount;

    public ItemInfo(ItemStack stack) {
        this(stack.getType(), stack.getDurability(), stack.getAmount());
    }

    public ItemInfo(Material id, short damage, int amount) {
        this.id = id;
        this.damage = damage;
        this.amount = amount;
    }

    public ItemStack toItemStack() {
        return new ItemStack(id, amount, damage);
    }
}
