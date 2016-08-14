package org.appledash.saneeconomy.utils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

/**
 * Created by appledash on 7/19/16.
 * Blackjack is still best pony.
 */
public class PlayerUtils {
    /**
     * Get an online or offline player from Bukkit.
     * This is guaranteed to be a player who has played before, but is not guaranteed to be currently online.
     * @param playerName The player's name
     * @return OfflinePlayer object, or null if never played
     */
    public static OfflinePlayer getOfflinePlayer(String playerName) {
        OfflinePlayer player = Bukkit.getServer().getPlayer(playerName);

        if (player == null) {
            player = Bukkit.getServer().getOfflinePlayer(playerName);
        }

        if ((player != null) && !player.hasPlayedBefore()) {
            return null;
        }

        return player;
    }
}
