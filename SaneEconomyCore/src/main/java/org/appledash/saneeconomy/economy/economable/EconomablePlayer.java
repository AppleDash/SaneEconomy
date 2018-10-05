package org.appledash.saneeconomy.economy.economable;

import org.bukkit.OfflinePlayer;

/**
 * Created by appledash on 7/19/16.
 * Blackjack is still best pony.
 */
public class EconomablePlayer implements Economable {
    private final OfflinePlayer handle;

    public EconomablePlayer(OfflinePlayer handle) {
        this.handle = handle;
    }

    @Override
    public String getName() {
        return handle.getName();
    }

    @Override
    public String getUniqueIdentifier() {
        return "player:" + handle.getUniqueId();
    }

    @Override
    public OfflinePlayer tryCastToPlayer() {
        return this.handle;
    }
}
