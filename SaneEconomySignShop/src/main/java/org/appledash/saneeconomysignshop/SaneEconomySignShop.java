package org.appledash.saneeconomysignshop;

import org.appledash.saneeconomy.ISaneEconomy;
import org.appledash.saneeconomysignshop.listeners.SignChangeListener;
import org.appledash.saneeconomysignshop.signshop.SignShopManager;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by appledash on 10/2/16.
 * Blackjack is still best pony.
 */
public class SaneEconomySignShop extends JavaPlugin {
    private ISaneEconomy saneEconomy;
    private final SignShopManager signShopManager = new SignShopManager();

    @Override
    public void onEnable() {
        if (!getServer().getPluginManager().isPluginEnabled("SaneEconomy")) {
            getLogger().severe("SaneEconomy is not enabled on this server - something is wrong here!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        saneEconomy = (ISaneEconomy)getServer().getPluginManager().getPlugin("SaneEconomy");

        // If it's stupid but it works... it's probably still stupid.
        getLogger().info(String.format("Hooked into SaneEconomy version %s.", ((Plugin)saneEconomy).getDescription().getVersion()));

        signShopManager.loadSignShops();
        getServer().getPluginManager().registerEvents(new SignChangeListener(this), this);
    }

    public SignShopManager getSignShopManager() {
        return signShopManager;
    }

    public ISaneEconomy getSaneEconomy() {
        return saneEconomy;
    }
}
