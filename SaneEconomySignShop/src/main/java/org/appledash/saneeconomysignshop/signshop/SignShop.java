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
    private final int quantity;
    private final double buyPrice;
    private final double sellPrice;

    public SignShop(UUID ownerUuid, Location location, Material item, int quantity, double buyPrice, double sellPrice) {
        if (ownerUuid == null || location == null || item == null) {
            throw new IllegalArgumentException("ownerUuid, location, and item must not be null.");
        }

        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }

        this.ownerUuid = ownerUuid;
        this.location = location;
        this.item = item;
        this.quantity = quantity;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
    }

    /**
     * Get the in-world Location of this SignShop
     * @return Location
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Get the type of item this SignShop is selling
     * @return Material representing item/block type
     */
    public Material getItem() {
        return item;
    }

    /**
     * Get the price that the player can buy this item from the server for
     * @return Buy price for this.getQuantity() items
     */
    public double getBuyPrice() {
        return buyPrice;
    }

    /**
     * Get the price that the player can sell this item to the server for
     * @return Buy price for this.getQuantity() items
     */
    public double getSellPrice() {
        return sellPrice;
    }

    /**
     * Get the price that the player can buy a specific number of this item from the server for.
     * Scales based on the defined price for the defined quantity.
     * @param quantity Quantity of items to price
     * @return Price to buy that number of items at this shop
     */
    public double getBuyPrice(int quantity) {
        return Math.ceil(this.buyPrice * (quantity / this.quantity)); // TODO: Is this okay?
    }

    /**
     * Get the price that the player can sell a specific number of this item to the server for.
     * Scales based on the defined price for the defined quantity.
     * @param quantity Quantity of items to price
     * @return Price to sell that number of items at this shop
     */
    public double getSellPrice(int quantity) {
        return Math.floor(this.sellPrice * (quantity / this.quantity)); // TODO: Is this okay?
    }

    /**
     * Check if anyone can buy items from this shop
     * @return True if they can, false if they can't
     */
    public boolean canBuy() {
        return buyPrice >= 0;
    }

    /**
     * Check if anyone can sell items to this shop
     * @return True if they can, false if they can't
     */
    public boolean canSell() {
        return sellPrice >= 0;
    }

    /**
     * Get the UUID of the player that created this SignShop
     * @return UUID
     */
    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    /**
     * Get the number of items that this shop will sell
     * @return Number of items
     */
    public int getQuantity() {
        return quantity;
    }
}
