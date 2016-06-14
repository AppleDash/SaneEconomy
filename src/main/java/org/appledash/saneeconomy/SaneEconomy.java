package org.appledash.saneeconomy;

import org.appledash.saneeconomy.command.SaneEconomyCommand;
import org.appledash.saneeconomy.command.type.BalanceCommand;
import org.appledash.saneeconomy.command.type.PayCommand;
import org.appledash.saneeconomy.economy.Currency;
import org.appledash.saneeconomy.economy.EconomyManager;
import org.appledash.saneeconomy.economy.backend.EconomyStorageBackend;
import org.appledash.saneeconomy.economy.backend.type.EconomyStorageBackendFlatfile;
import org.appledash.saneeconomy.listeners.JoinQuitListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by AppleDash on 6/13/2016.
 * Blackjack is still best pony.
 */
public class SaneEconomy extends JavaPlugin {
    private static SaneEconomy instance;
    private EconomyManager economyManager;

    private static final Map<String, SaneEconomyCommand> COMMANDS = new HashMap<String, SaneEconomyCommand>() {{
        put("balance", new BalanceCommand());
        put("ecoadmin", new BalanceCommand());
        put("pay", new PayCommand());
    }};

    public SaneEconomy() {
        instance = this;
    }

    @Override
    public void onEnable() {
        new File(getDataFolder(), "config.yml").delete();
        saveDefaultConfig();
        getLogger().setLevel(Level.ALL);
        getLogger().info("Initializing currency...");

        Currency currency = Currency.fromConfig(getConfig(), "currency");

        getLogger().info("Initialized currency: " + currency.getPluralName());

        EconomyStorageBackend backend;

        getLogger().info("Initializing economy storage backend...");
        String backendType = getConfig().getString("backend.type");

        /* Flatfile database, currently only supported. */
        if (backendType.equalsIgnoreCase("flatfile")) {
            String backendFileName = getConfig().getString("backend.file", "economy.db");
            File backendFile = new File(getDataFolder(), backendFileName);
            backend = new EconomyStorageBackendFlatfile(backendFile);
            getLogger().info("Initialized flatfile backend with file " + backendFile.getAbsolutePath());
        } else {
            getLogger().severe("Unknown storage backend " + backendType + "!");
            shutdown();

            return;
        }

        economyManager = new EconomyManager(currency, backend);

        loadCommands();

        getLogger().info("Initializing listeners...");
        getServer().getPluginManager().registerEvents(new JoinQuitListener(this), this);
        getLogger().info("Initialized listeners!");
    }

    private void loadCommands() {
        getLogger().info("Initializing commands...");
        COMMANDS.forEach((name, command) -> getCommand(name).setExecutor(command));
        getLogger().info("Commands initialized!");
    }

    private void shutdown(){
        getServer().getPluginManager().disablePlugin(this);
    }

    /**
     * Get the active EconomyManager.
     * @return EconomyManager
     */
    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    /**
     * Get the current plugin instance.
     * @return Instance
     */
    public static SaneEconomy getInstance() {
        return instance;
    }

    /**
     * Get the logger for the plugin.
     * @return Plugin logger.
     */
    public static Logger logger(){
        return getInstance().getLogger();
    }
}
