package org.appledash.saneeconomy.utils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

/**
 * Created by appledash on 7/19/16.
 * Blackjack is still best pony.
 */
public class PlayerUtils {
    /**
     * Get an online or offline player from Bukkit.
     * This is guaranteed to be a player who has played before, but is not guaranteed to be currently online.
     * @param playerNameOrUUID The player's name or UUID
     * @return OfflinePlayer object, or null if never played
     */
    public static OfflinePlayer getOfflinePlayer(String playerNameOrUUID) {
        OfflinePlayer player = tryGetFromUUID(playerNameOrUUID);

        if (player != null && (player.hasPlayedBefore() || player.isOnline())) {
            return player;
        }

        player = Bukkit.getServer().getPlayer(playerNameOrUUID);

        if (player == null) {
            player = Bukkit.getServer().getOfflinePlayer(playerNameOrUUID);
        }

        if ((player != null) && (!player.hasPlayedBefore() && !player.isOnline())) {
            return null;
        }

        return player;
    }

    private static OfflinePlayer tryGetFromUUID(String possibleUUID) {
        UUID uuid;
        OfflinePlayer player;

        try {
            uuid = UUID.fromString(possibleUUID);
        } catch (IllegalArgumentException ignored) {
            return null;
        }

        player = Bukkit.getServer().getPlayer(uuid);

        if (player != null) {
            return player;
        }

        return Bukkit.getServer().getOfflinePlayer(uuid);
    }
}
