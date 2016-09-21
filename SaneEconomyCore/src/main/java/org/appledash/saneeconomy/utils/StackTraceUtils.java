package org.appledash.saneeconomy.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by appledash on 9/21/16.
 * Blackjack is best pony.
 */
public class StackTraceUtils {
    public static void what() {
        for (StackTraceElement elem : Thread.currentThread().getStackTrace()) {
            String className = elem.getClassName();

            Plugin plugin = getPlugin(className);

            if (plugin != null) {
                System.out.printf("Calling plugin seems to be %s (%s)\n", plugin.getName(), className);
            }
        }
    }

    private static Class classFromName(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private static Plugin getPlugin(String className) {
        Class clazz = classFromName(className);

        if (clazz == null) {
            return null;
        }

        if (!JavaPlugin.class.isAssignableFrom(clazz)) {
            return null;
        }

        for (Plugin plugin : Bukkit.getServer().getPluginManager().getPlugins()) {
            if (clazz.isInstance(plugin)) {
                return plugin;
            }
        }

        return null;
    }
}
