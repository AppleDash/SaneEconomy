package org.appledash.saneeconomysignshop;

import org.appledash.saneeconomy.ISaneEconomy;
import org.appledash.saneeconomysignshop.listeners.BreakListener;
import org.appledash.saneeconomysignshop.listeners.InteractListener;
import org.appledash.saneeconomysignshop.listeners.SignChangeListener;
import org.appledash.saneeconomysignshop.signshop.SignShopManager;
import org.appledash.saneeconomysignshop.signshop.storage.SignShopStorageJSON;
import org.appledash.saneeconomysignshop.util.ItemDatabase;
import org.appledash.saneeconomysignshop.util.LimitManager;
import org.appledash.sanelib.SanePlugin;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.InputStreamReader;

/**
 * Created by appledash on 10/2/16.
 * Blackjack is still best pony.
 */
public class SaneEconomySignShop extends SanePlugin {
    private ISaneEconomy saneEconomy;
    private final SignShopManager signShopManager = new SignShopManager(new SignShopStorageJSON(new File(this.getDataFolder(), "shops.json")));
    private final LimitManager limitManager = new LimitManager();

    @Override
    public void onEnable() {
        if (!this.getServer().getPluginManager().isPluginEnabled("SaneEconomy")) {
            this.getLogger().severe("SaneEconomy is not enabled on this server - something is wrong here!");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        super.onEnable();

        ItemDatabase.initItemDB();

        this.saneEconomy = (ISaneEconomy) this.getServer().getPluginManager().getPlugin("SaneEconomy");

        // If it's stupid but it works... it's probably still stupid.
        this.getLogger().info(String.format("Hooked into SaneEconomy version %s.", ((Plugin) this.saneEconomy).getDescription().getVersion()));

        this.saveDefaultConfig();

        this.limitManager.loadLimits(YamlConfiguration.loadConfiguration(new InputStreamReader(this.getClass().getResourceAsStream("/limits.yml")))); // Always load from JAR
        this.signShopManager.loadSignShops();

        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, this.limitManager::incrementLimitsHourly, 0, 20 * 60 * 60);

        this.getServer().getPluginManager().registerEvents(new SignChangeListener(this), this);
        this.getServer().getPluginManager().registerEvents(new InteractListener(this), this);
        this.getServer().getPluginManager().registerEvents(new BreakListener(this), this);
    }

    public SignShopManager getSignShopManager() {
        return this.signShopManager;
    }

    public ISaneEconomy getSaneEconomy() {
        return this.saneEconomy;
    }

    public LimitManager getLimitManager() {
        return this.limitManager;
    }
}
