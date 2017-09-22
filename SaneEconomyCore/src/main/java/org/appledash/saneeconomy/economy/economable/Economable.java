package org.appledash.saneeconomy.economy.economable;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by appledash on 7/19/16.
 * Blackjack is still best pony.
 */
public interface Economable {
    Economable CONSOLE = new EconomableConsole();
    Economable PLUGIN = new EconomablePlugin();

    String getUniqueIdentifier();
    default OfflinePlayer tryCastToPlayer() {
        return null;
    }

    static Economable wrap(OfflinePlayer player) {
        return new EconomablePlayer(player);
    }

    static Economable wrap(CommandSender sender) {
        if (sender instanceof OfflinePlayer) {
            return wrap(((OfflinePlayer) sender));
        }

        return CONSOLE;
    }

    static Economable wrap(Player player) {
        return wrap(((OfflinePlayer) player));
    }

    static Economable wrap(String identifier) {
        if (identifier.startsWith("faction-")) {
            return new EconomableFaction(identifier.replace("faction-", ""));
        }

        return new EconomableGeneric("generic:" + identifier);
    }
}
