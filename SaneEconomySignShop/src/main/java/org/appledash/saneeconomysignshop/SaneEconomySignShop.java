package org.appledash.saneeconomysignshop;

import org.appledash.saneeconomysignshop.listeners.SignChangeListener;
import org.appledash.saneeconomysignshop.signshop.SignShopManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by appledash on 10/2/16.
 * Blackjack is still best pony.
 */
public class SaneEconomySignShop extends JavaPlugin {
    private final SignShopManager signShopManager = new SignShopManager();

    @Override
    public void onEnable() {
        signShopManager.loadSignShops();
        getServer().getPluginManager().registerEvents(new SignChangeListener(this), this);
    }
}
