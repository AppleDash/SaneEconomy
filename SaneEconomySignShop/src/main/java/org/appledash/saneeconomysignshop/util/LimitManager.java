package org.appledash.saneeconomysignshop.util;

import org.appledash.saneeconomysignshop.signshop.ShopTransaction;
import org.appledash.saneeconomysignshop.signshop.ShopTransaction.TransactionDirection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by appledash on 1/1/17.
 * Blackjack is still best pony.
 */
public class LimitManager {
    private final Map<TransactionDirection, Map<ItemInfo, ItemLimits>> itemLimits = new DefaultHashMap<>(() -> new DefaultHashMap<>(() -> ItemLimits.DEFAULT));
    // This is a slightly complex data structure. It works like this:
    // It's a map of (limit types to (maps of players to (maps of materials to the remaning limit))).
    // All the TransactionDirections defaults to an empty map, which defaults to an empty map, which defaults to 0.
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final Map<TransactionDirection, Map<UUID, Map<ItemInfo, Integer>>> playerLimits = new DefaultHashMap<>(() -> new DefaultHashMap<>(() -> new DefaultHashMap<>(() -> 0)));

    public int getRemainingLimit(Player player, TransactionDirection type, ItemInfo stack) {
        return playerLimits.get(type).get(player.getUniqueId()).get(stack);
    }

    public void setRemainingLimit(Player player, TransactionDirection type, ItemInfo stack, int limit) {
        if (playerLimits.get(type).get(player.getUniqueId()).get(stack) == -1) {
            return;
        }

        limit = Math.min(limit, itemLimits.get(type).get(stack).getLimit());
        limit = Math.max(0, limit);

        playerLimits.get(type).get(player.getUniqueId()).put(stack, limit);
    }

    public boolean shouldAllowTransaction(ShopTransaction transaction) {
        return getRemainingLimit(transaction.getPlayer(), transaction.getDirection(), transaction.getItem()) >= transaction.getQuantity();
    }

    public void incrementLimitsHourly() {
        // For every limit type
            // For every player
                // For every limit
                // Increment limit by the limit for the specific direction and item.

        playerLimits.forEach((transactionDirection, playerToLimit) -> {
            playerToLimit.forEach((playerUuid, itemToLimit) -> {
                Map<ItemInfo, Integer> newLimits = new HashMap<>();

                itemToLimit.forEach((itemInfo, currentLimit) -> {
                    newLimits.put(itemInfo, currentLimit + (itemLimits.get(transactionDirection).get(itemInfo).getHourlyGain()));
                });

                itemToLimit.putAll(newLimits);
            });
        });
    }

}
