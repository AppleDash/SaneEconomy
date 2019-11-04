package org.appledash.saneeconomymobkills;

import org.appledash.saneeconomy.SaneEconomy;
import org.appledash.saneeconomymobkills.listeners.EntityDamageListener;
import org.appledash.sanelib.SanePlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by appledash on 12/27/16.
 * Blackjack is still best pony.
 */
public class SaneEconomyMobKills extends SanePlugin {
    private SaneEconomy saneEconomy;
    private final Map<String, Double> killAmounts = new HashMap<>();

    @Override
    public void onEnable() {
        this.saneEconomy = (SaneEconomy) this.getServer().getPluginManager().getPlugin("SaneEconomy");
        super.onEnable();

        YamlConfiguration amountsConfig;

        if (!(new File(this.getDataFolder(), "amounts.yml").exists())) {
            amountsConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(this.getClass().getResourceAsStream("/amounts.yml")));
            try {
                amountsConfig.save(new File(this.getDataFolder(), "amounts.yml"));
            } catch (IOException e) {
                throw new RuntimeException("Failed to save amounts.yml to plugin data folder!");
            }
        } else {
            amountsConfig = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "amounts.yml"));
        }

        for (String entityTypeName : amountsConfig.getKeys(false)) {
            double value = amountsConfig.getDouble(entityTypeName);
            this.killAmounts.put(entityTypeName, value);
        }

        this.getServer().getPluginManager().registerEvents(new EntityDamageListener(this), this);
    }

    public SaneEconomy getSaneEconomy() {
        return this.saneEconomy;
    }

    public Map<String, Double> getKillAmounts() {
        return this.killAmounts;
    }
}
