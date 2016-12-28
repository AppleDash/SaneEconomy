package org.appledash.saneeconomymobkills;

import org.appledash.saneeconomy.SaneEconomy;
import org.appledash.saneeconomymobkills.listeners.EntityDamageListener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by appledash on 12/27/16.
 * Blackjack is still best pony.
 */
public class SaneEconomyMobKills extends JavaPlugin {
    private SaneEconomy saneEconomy;

    @Override
    public void onEnable() {
        saneEconomy = (SaneEconomy)getServer().getPluginManager().getPlugin("SaneEconomy");
        getServer().getPluginManager().registerEvents(new EntityDamageListener(this), this);
    }

    public SaneEconomy getSaneEconomy() {
        return saneEconomy;
    }
}
