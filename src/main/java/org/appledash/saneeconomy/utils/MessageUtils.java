package org.appledash.saneeconomy.utils;

import net.md_5.bungee.api.ChatColor;
import org.appledash.saneeconomy.SaneEconomy;
import org.bukkit.command.CommandSender;

/**
 * Created by AppleDash on 6/13/2016.
 * Blackjack is still best pony.
 */
public class MessageUtils {
    public static void sendMessage(CommandSender sender, String fmt, String... args) {
        String prefix = ChatColor.translateAlternateColorCodes('&', SaneEconomy.getInstance().getConfig().getString("chat.prefix", "[SaneEcon] "));;
        sender.sendMessage(prefix + String.format(fmt, args));
    }
}
