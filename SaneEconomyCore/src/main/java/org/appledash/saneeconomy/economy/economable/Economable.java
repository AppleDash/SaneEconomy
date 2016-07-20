package org.appledash.saneeconomy.economy.economable;

import org.bukkit.OfflinePlayer;

/**
 * Created by appledash on 7/19/16.
 * Blackjack is still best pony.
 */
public interface Economable {
    String getUniqueIdentifier();

    static Economable wrap(OfflinePlayer player) {
        return new EconomablePlayer(player);
    }

    static Economable wrap(String playerName) {
        return () -> "wtf:" + playerName;
    }
}
