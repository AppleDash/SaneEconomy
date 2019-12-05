package org.appledash.saneeconomysignshop.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.Serializable;
import java.util.Objects;

/**
 * Created by appledash on 11/3/16.
 * Blackjack is still best pony.
 */
public class ItemInfo implements Serializable {
    private final Material material;
    private final short damage;
    private final int amount;

    public ItemInfo(ItemStack stack) {
        this(stack.getType(), stack.getDurability(), stack.getAmount());
    }

    public ItemInfo(Material material, short damage, int amount) {
        this.material = material;
        this.damage = damage;
        this.amount = amount;
    }

    public ItemStack toItemStack() {
        return new ItemStack(this.material, this.amount, this.damage);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ItemInfo)) {
            return false;
        }

        ItemInfo other = ((ItemInfo) o);

        return (other.material == this.material) && (other.damage == this.damage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.material, this.damage);
    }
}
