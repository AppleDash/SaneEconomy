package org.appledash.saneeconomy.utils;

import net.md_5.bungee.api.ChatColor;
import org.appledash.saneeconomy.SaneEconomy;
import org.bukkit.command.CommandSender;

/**
 * Created by AppleDash on 6/13/2016.
 * Blackjack is still best pony.
 */
public class MessageUtils {
    /**
     * Send a formatted chat message to the given target.
     * This message will have the prefix defined in SaneEconomy's config file.
     * @param target Target CommandSender
     * @param fmt String#format format
     * @param args String#format args
     */
    public static void sendMessage(CommandSender target, String fmt, Object... args) {
        String prefix = ChatColor.translateAlternateColorCodes('&', SaneEconomy.getInstance().getConfig().getString("chat.prefix", ""));
        target.sendMessage(prefix + String.format(fmt, (Object[])args));
    }
}
