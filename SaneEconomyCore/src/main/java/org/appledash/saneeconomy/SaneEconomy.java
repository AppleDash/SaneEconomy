package org.appledash.saneeconomy;

import com.google.common.base.Strings;
import org.appledash.saneeconomy.command.SaneEconomyCommand;
import org.appledash.saneeconomy.command.type.*;
import org.appledash.saneeconomy.economy.Currency;
import org.appledash.saneeconomy.economy.EconomyManager;
import org.appledash.saneeconomy.economy.backend.EconomyStorageBackend;
import org.appledash.saneeconomy.economy.backend.type.EconomyStorageBackendFlatfile;
import org.appledash.saneeconomy.economy.backend.type.EconomyStorageBackendMySQL;
import org.appledash.saneeconomy.economy.economable.EconomableGeneric;
import org.appledash.saneeconomy.economy.logger.TransactionLogger;
import org.appledash.saneeconomy.listeners.JoinQuitListener;
import org.appledash.saneeconomy.updates.GithubVersionChecker;
import org.appledash.saneeconomy.utils.DatabaseCredentials;
import org.appledash.saneeconomy.utils.I18n;
import org.appledash.saneeconomy.vault.VaultHook;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by AppleDash on 6/13/2016.
 * Blackjack is still best pony.
 *
 * FIXME: Why is the backend and config loading so complex and why is it even in this class?
 */
public class SaneEconomy extends JavaPlugin implements ISaneEconomy {
    private static SaneEconomy instance;
    private EconomyManager economyManager;
    private VaultHook vaultHook;
    private TransactionLogger transactionLogger;

    private final Map<String, SaneEconomyCommand> COMMANDS = new HashMap<String, SaneEconomyCommand>() {{
        put("balance", new BalanceCommand(SaneEconomy.this));
        put("ecoadmin", new EconomyAdminCommand(SaneEconomy.this));
        put("pay", new PayCommand(SaneEconomy.this));
        put("saneeconomy", new SaneEcoCommand(SaneEconomy.this));
        put("balancetop", new BalanceTopCommand(SaneEconomy.this));
    }};

    public SaneEconomy() {
        instance = this;
    }

    @Override
    public void onEnable() {
        loadConfig();

        if (!loadEconomyBackend()) { /* Invalid backend type or connection error of some sort */
            shutdown();
            return;
        }

        loadCommands();
        loadListeners();

        if (getServer().getPluginManager().isPluginEnabled("Vault")) {
            vaultHook = new VaultHook(this);
            vaultHook.hook();
            getLogger().info("Hooked into Vault.");
        } else {
            getLogger().info("Not hooking into Vault because it isn't loaded.");
        }

        getServer().getScheduler().scheduleAsyncDelayedTask(this, GithubVersionChecker::checkUpdateAvailable);

        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            economyManager.getBackend().reloadTopPlayerBalances();
        }, 0, (20 * 300) /* Update baltop every 5 minutes */);
        I18n.getInstance().loadTranslations();
    }

    @Override
    public void onDisable() {
        if (vaultHook != null) {
            vaultHook.unhook();
        }

        economyManager.getBackend().waitUntilFlushed();
    }

    private void loadConfig() {
        File configFile = new File(getDataFolder(), "config.yml");

        if (configFile.exists() && getConfig().getBoolean("debug", false)) {
            getLogger().info("Resetting configuration to default since debug == true.");
            configFile.delete();
            saveDefaultConfig();
            reloadConfig();
            getConfig().set("debug", true);
            saveConfig();
        } else {
            saveDefaultConfig();
            reloadConfig();
        }
    }

    private boolean loadEconomyBackend() {
        getLogger().info("Initializing currency...");
        Currency currency = Currency.fromConfig(getConfig(), "currency");
        getLogger().info("Initialized currency: " + currency.getPluralName());

        getLogger().info("Initializing economy storage backend...");

        EconomyStorageBackend backend = loadBackend(getConfig().getConfigurationSection("backend"));

        if (backend == null) {
            getLogger().severe("Failed to load backend!");
            return false;
        }

        getLogger().info("Performing initial data load...");
        backend.reloadDatabase();
        getLogger().info("Data loaded!");

        if (!Strings.isNullOrEmpty(getConfig().getString("old-backend.type", null))) {
            getLogger().info("Old backend detected, converting... (This may take a minute or two.)");
            EconomyStorageBackend oldBackend = loadBackend(getConfig().getConfigurationSection("old-backend"));
            if (oldBackend == null) {
                getLogger().severe("Failed to load old backend!");
                return false;
            }

            oldBackend.reloadDatabase();
            convertBackends(oldBackend, backend);
            getLogger().info("Data converted, removing old config section.");
            getConfig().set("old-backend", null);
            saveConfig();
        }

        economyManager = new EconomyManager(this, currency, backend);

        return true;
    }

    private EconomyStorageBackend loadBackend(ConfigurationSection config) {
        EconomyStorageBackend backend;
        String backendType = config.getString("type");

        if (backendType.equalsIgnoreCase("flatfile")) {
            String backendFileName = config.getString("file", "economy.db");
            File backendFile = new File(getDataFolder(), backendFileName);
            backend = new EconomyStorageBackendFlatfile(backendFile);
            getLogger().info("Initialized flatfile backend with file " + backendFile.getAbsolutePath());
        } else if (backendType.equalsIgnoreCase("mysql")) {
            EconomyStorageBackendMySQL mySQLBackend = new EconomyStorageBackendMySQL(loadCredentials(config));

            backend = mySQLBackend;

            getLogger().info("Initialized MySQL backend.");
            getLogger().info("Testing connection...");
            if (!mySQLBackend.testConnection()) {
                getLogger().severe("MySQL connection failed - cannot continue!");
                return null;
            }

            getLogger().info("Connection successful!");
        } else {
            getLogger().severe("Unknown storage backend " + backendType + "!");
            return null;
        }

        return backend;
    }

    private void convertBackends(EconomyStorageBackend old, EconomyStorageBackend newer) {
        old.getAllBalances().forEach((uniqueId, balance) -> {
            newer.setBalance(new EconomableGeneric(uniqueId), balance);
        });
        newer.waitUntilFlushed();
    }

    private TransactionLogger loadTransactionLogger() {
        if (!getConfig().getBoolean("transaction-logger.enabled", false)) {
            return null;
        }

        return null;
    }

    private DatabaseCredentials loadCredentials(ConfigurationSection config) {
        String backendHost = config.getString("host");
        int backendPort = config.getInt("port", 3306);
        String backendDb = config.getString("database");
        String backendUser = config.getString("username");
        String backendPass = config.getString("password");

        return new DatabaseCredentials(
                backendHost, backendPort, backendUser, backendPass, backendDb
        );
    }

    private void loadCommands() {
        getLogger().info("Initializing commands...");
        COMMANDS.forEach((name, command) -> getCommand(name).setExecutor(command));
        getLogger().info("Initialized commands.");
    }

    private void loadListeners() {
        getLogger().info("Initializing listeners...");
        getServer().getPluginManager().registerEvents(new JoinQuitListener(this), this);
        getLogger().info("Initialized listeners.");
    }

    private void shutdown(){
        getServer().getPluginManager().disablePlugin(this);
    }

    /**
     * Get the active EconomyManager.
     * @return EconomyManager
     */
    @Override
    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    /**
     * Check whether transactions should be logged.
     * @return True if transactions should be logged, false otherwise.
     */
    @Override
    public boolean shouldLogTransactions() {
        return transactionLogger != null;
    }

    /**
     * Get the active TransactionLogger.
     * @return TransactionLogger, if there is one.
     */
    @Override
    public TransactionLogger getTransactionLogger() {
        return transactionLogger;
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
        return instance.getLogger();
    }
}
