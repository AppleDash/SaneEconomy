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

    static Economable wrap(String identifier) {
        if (identifier.startsWith("faction-")) {
            return new EconomableFaction(identifier.replace("faction-", ""));
        }

        return new EconomableGeneric("generic:" + identifier);
    }
}
