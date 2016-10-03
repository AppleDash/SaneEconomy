package org.appledash.saneeconomysignshop.signshop;

import org.bukkit.Location;
import org.bukkit.Material;

import java.util.UUID;

/**
 * Created by appledash on 10/2/16.
 * Blackjack is still best pony.
 */
public class SignShop {
    private final UUID ownerUuid;
    private final Location location;
    private final Material item;
    private final double buyAmount;
    private final double sellAmount;

    public SignShop(UUID ownerUuid, Location location, Material item, double buyAmount, double sellAmount) {
        this.ownerUuid = ownerUuid;
        this.location = location;
        this.item = item;
        this.buyAmount = buyAmount;
        this.sellAmount = sellAmount;
    }

    public Location getLocation() {
        return location;
    }

    public Material getItem() {
        return item;
    }

    public double getBuyAmount() {
        return buyAmount;
    }

    public double getSellAmount() {
        return sellAmount;
    }

    public boolean canBuy() {
        return buyAmount >= 0;
    }

    public boolean canSell() {
        return sellAmount >= 0;
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }
}
