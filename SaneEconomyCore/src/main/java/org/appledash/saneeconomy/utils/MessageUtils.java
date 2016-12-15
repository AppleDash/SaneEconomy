package org.appledash.saneeconomy.utils;

import org.appledash.saneeconomy.SaneEconomy;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.appledash.saneeconomy.utils.I18n._;

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
        fmt = _(fmt);
        String prefix = ChatColor.translateAlternateColorCodes('&', SaneEconomy.getInstance().getConfig().getString("chat.prefix", ""));
        target.sendMessage(prefix + String.format(fmt, (Object[])args));
    }

    public static String indexedFormat(String fmt, String... arguments) {
        Matcher m = Pattern.compile("\\{([0-9]+)\\}").matcher(fmt);
        StringBuffer formatted = new StringBuffer();

        while (m.find()) {
            int index = Integer.valueOf(m.group(1)) - 1;

            if (index > arguments.length - 1 || index < 0) {
                throw new IllegalArgumentException("Index must be within the range of the given arguments.");
            }

            m.appendReplacement(formatted, arguments[index]);
        }

        m.appendTail(formatted);

        return formatted.toString();
    }
}
