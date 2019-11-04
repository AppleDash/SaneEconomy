package org.appledash.saneeconomysignshop.util;

import org.appledash.saneeconomysignshop.signshop.ShopTransaction;
import org.appledash.saneeconomysignshop.signshop.ShopTransaction.TransactionDirection;
import org.appledash.saneeconomysignshop.util.ItemDatabase.InvalidItemException;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Created by appledash on 1/1/17.
 * Blackjack is still best pony.
 */
public class LimitManager {
    private static final Logger LOGGER = Logger.getLogger("LimitManager");
    // private final Map<ItemInfo, ItemLimits> buyItemLimits = new DefaultHashMap<ItemInfo, ItemLimits>(() -> ItemLimits.DEFAULT);
    private final Map<ItemInfo, ItemLimits> sellItemLimits = new DefaultHashMap<>(() -> ItemLimits.DEFAULT);
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final Map<UUID, Map<ItemInfo, Integer>> sellPlayerLimits = new DefaultHashMap<>(() -> new DefaultHashMap<>((info) -> sellItemLimits.get(info).getLimit()));
    // private final Map<TransactionDirection, Map<ItemInfo, ItemLimits>> itemLimits = new DefaultHashMap<>(() -> new DefaultHashMap<>(() -> ItemLimits.DEFAULT));
    // This is a slightly complex data structure. It works like this:
    // It's a map of (limit types to (maps of players to (maps of materials to the remaining limit))).
    // All the TransactionDirections defaults to an empty map, which defaults to an empty map, which defaults to 0.
    // @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    // private final Map<TransactionDirection, Map<UUID, Map<ItemInfo, Integer>>> playerLimits = new DefaultHashMap<>(() -> new DefaultHashMap<>(() -> new DefaultHashMap<>(() -> 0)));

    public int getRemainingLimit(Player player, TransactionDirection type, ItemInfo stack) {
        if (type == TransactionDirection.SELL) {
            return sellPlayerLimits.get(player.getUniqueId()).get(stack);
        }

        throw new IllegalArgumentException("Don't know how to get limits for that TransactionDirection!");
    }

    public void setRemainingLimit(Player player, TransactionDirection type, ItemInfo stack, int limit) {
        if (type == TransactionDirection.SELL) {
            if (sellPlayerLimits.get(player.getUniqueId()).get(stack) == -1) {
                return;
            }

            limit = Math.min(limit, sellItemLimits.get(stack).getLimit());
            limit = Math.max(0, limit);

            sellPlayerLimits.get(player.getUniqueId()).put(stack, limit);
            return;
        }

        throw new IllegalArgumentException("Don't know how to set limits for that TransactionDirection!");
    }

    public boolean shouldAllowTransaction(ShopTransaction transaction) {
        // System.out.printf("Limit: %d, quantity: %d\n", limit, transaction.getQuantity());
        return getRemainingLimit(transaction.getPlayer(), transaction.getDirection(), transaction.getItem()) >= transaction.getQuantity();
    }

    public void incrementLimitsHourly() {
        // For every limit type
        // For every player
        // For every limit
        // Increment limit by the limit for the specific direction and item.

        sellPlayerLimits.forEach((playerUuid, itemToLimit) -> {
            Map<ItemInfo, Integer> newLimits = new HashMap<>();

            itemToLimit.forEach((itemInfo, currentLimit) ->
                                newLimits.put(itemInfo, currentLimit + (sellItemLimits.get(itemInfo).getHourlyGain())));

            itemToLimit.putAll(newLimits);
        });
    }

    public void loadLimits(ConfigurationSection config) {
        for (Map<?, ?> map : config.getMapList("sell")) {
            String itemName = String.valueOf(map.get("item"));
            int sellLimit = Integer.valueOf(String.valueOf(map.get("limit")));
            int hourlyGain = Integer.valueOf(String.valueOf(map.get("gain")));
            ItemStack stack;

            try {
                stack = ItemDatabase.parseGive(itemName);
            } catch (InvalidItemException e) {
                LOGGER.warning(String.format("You tried to load the item '%s' in limits.yml, but I have no idea what that is.", map.get("item")));
                continue;
            }


            ItemInfo itemInfo = new ItemInfo(stack);

            sellItemLimits.put(itemInfo, new ItemLimits(sellLimit, hourlyGain));
        }
    }

}
