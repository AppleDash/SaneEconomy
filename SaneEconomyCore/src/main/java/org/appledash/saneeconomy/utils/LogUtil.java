package org.appledash.saneeconomy.utils;

import org.bukkit.ChatColor;

public class LogUtil {
    private LogUtil() {}

    public static String getHeading() { return ChatColor.YELLOW + "[" + ChatColor.AQUA + "SaneEconomy" + ChatColor.YELLOW + "] " + ChatColor.RESET; }
}
