package org.appledash.saneeconomy;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import java.io.File;
import java.util.*;
import java.util.logging.Level;
import org.appledash.saneeconomy.command.*;
import org.appledash.saneeconomy.economy.EconomyManager;
import org.appledash.saneeconomy.economy.backend.EconomyStorageBackend;
import org.appledash.saneeconomy.economy.backend.type.EconomyStorageBackendMySQL;
import org.appledash.saneeconomy.economy.logger.TransactionLogger;
import org.appledash.saneeconomy.event.SaneEconomyTransactionEvent;
import org.appledash.saneeconomy.listeners.JoinQuitListener;
import org.appledash.saneeconomy.updates.GithubVersionChecker;
import org.appledash.saneeconomy.utils.SaneEconomyConfiguration;
import org.appledash.saneeconomy.vault.VaultHook;
import org.appledash.sanelib.SanePlugin;
import org.appledash.sanelib.command.SaneCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Created by AppleDash on 6/13/2016.
 * Blackjack is still best pony.
 */
public class SaneEconomy implements ISaneEconomy {
    private EconomyManager economyManager;
    private VaultHook vaultHook;
    private TransactionLogger transactionLogger;
    private GithubVersionChecker versionChecker;

    private final Map<String, SaneCommand> COMMANDS = new HashMap<String, SaneCommand>() {{
        put("balance", new BalanceCommand(SaneEconomy.this));
        put("ecoadmin", new EconomyAdminCommand(SaneEconomy.this));
        put("pay", new PayCommand(SaneEconomy.this));
        put("saneeconomy", new SaneEcoCommand(SaneEconomy.this));
        put("balancetop", new BalanceTopCommand(SaneEconomy.this));
    }};

    private final SanePlugin plugin;
    private final boolean isBukkit;
    private static SaneEconomy instance;

    public SaneEconomy(SanePlugin plugin) {
        isBukkit = Bukkit.getName().equals("Bukkit") || Bukkit.getName().equals("CraftBukkit");
        this.plugin = plugin;
        this.instance = this;
    }

    public void onLoad() {}

    public void onEnable() {
        if (!loadConfig()) { /* Invalid backend type or connection error of some sort */
            shutdown();
            return;
        }

        if (plugin.getConfig().getBoolean("locale-override", false)) {
            Locale.setDefault(Locale.ENGLISH);
        }

        loadCommands();
        loadListeners();

        if (Bukkit.getServer().getPluginManager().isPluginEnabled("Vault")) {
            vaultHook = new VaultHook(this);
            vaultHook.hook();
            log(Level.INFO, "Hooked into Vault.");
        } else {
            log(Level.INFO, "Not hooking into Vault because it isn't loaded.");
        }

        if (plugin.getConfig().getBoolean("update-check", true)) {
            versionChecker = new GithubVersionChecker("SaneEconomyCore", plugin.getDescription().getVersion().replace("-SNAPSHOT", ""));
            Bukkit.getServer().getScheduler().runTaskAsynchronously(plugin, versionChecker::checkUpdateAvailable);
        }

        Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            economyManager.getBackend().reloadTopPlayerBalances();
        }, 0L, (20L * plugin.getConfig().getLong("economy.baltop-update-interval", 300L)) /* Update baltop every 5 minutes by default */);

        if (plugin.getConfig().getBoolean("multi-server-sync", false)) {
            Bukkit.getServer().getPluginManager().registerEvents(new Listener() {
                @EventHandler
                public void onTransaction(SaneEconomyTransactionEvent evt) { // Trust me, I'm a doctor.
                    OfflinePlayer[] playersToSync = { evt.getTransaction().getSender().tryCastToPlayer(), evt.getTransaction().getReceiver().tryCastToPlayer() };

                    Player fakeSender = Iterables.getFirst(Bukkit.getServer().getOnlinePlayers(), null);

                    if (fakeSender == null) {
                        return;
                    }

                    Arrays.stream(playersToSync).filter(p -> (p != null) && !p.isOnline()).forEach(p -> {
                        ByteArrayDataOutput bado = ByteStreams.newDataOutput();
                        bado.writeUTF("SaneEconomy");
                        bado.writeUTF("SyncPlayer");
                        bado.writeUTF(p.getUniqueId().toString());
                        fakeSender.sendPluginMessage(plugin, "BungeeCord", bado.toByteArray());
                    });
                }
            }, plugin);
            Bukkit.getServer().getMessenger().registerIncomingPluginChannel(plugin, "BungeeCord", (channel, player, bytes) -> {
                if (!channel.equals("BungeeCord")) {
                    return;
                }

                ByteArrayDataInput badi = ByteStreams.newDataInput(bytes);
                String subChannel = badi.readUTF();

                if (subChannel.equals("SaneEconomy")) {
                    String opCode = badi.readUTF();

                    if (opCode.equals("SyncPlayer")) {
                        String playerUuid = badi.readUTF();
                        this.economyManager.getBackend().reloadEconomable(String.format("player:%s", playerUuid), EconomyStorageBackend.EconomableReloadReason.CROSS_SERVER_SYNC);
                    } else {
                        log(Level.WARNING, "Invalid OpCode received on SaneEconomy plugin message channel: " + opCode);
                    }
                }
            });
            Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
        }
    }

    public void onDisable() {
        if (vaultHook != null) {
            log(Level.INFO, "Unhooking from Vault.");
            vaultHook.unhook();
        }

        this.flushEconomyManager();
    }

    private void flushEconomyManager() {
        if (this.economyManager != null) {
            log(Level.INFO, "Flushing database...");
            this.economyManager.getBackend().waitUntilFlushed();

            if (this.economyManager.getBackend() instanceof EconomyStorageBackendMySQL) {
                ((EconomyStorageBackendMySQL) economyManager.getBackend()).closeConnections();
                if (!((EconomyStorageBackendMySQL) economyManager.getBackend()).getConnection().getConnection().isFinished()) {
                    log(Level.WARNING, "SaneDatabase didn't terminate all threads, something weird is going on?");
                }
            }
        }
    }

    public boolean loadConfig() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");

        if (configFile.exists() && plugin.getConfig().getBoolean("debug", false)) {
            log(Level.INFO, "Resetting configuration to default since debug == true.");
            configFile.delete();
            plugin.saveDefaultConfig();
            plugin.reloadConfig();
            plugin.getConfig().set("debug", true);
            plugin.saveConfig();
        } else {
            if (!configFile.exists()) {
                plugin.saveDefaultConfig();
            }
            plugin.reloadConfig();
        }

        this.flushEconomyManager(); // If we're reloading the configuration, we flush the old economy manager first

        SaneEconomyConfiguration config = new SaneEconomyConfiguration(this);

        economyManager = config.loadEconomyBackend();
        transactionLogger = config.loadLogger();

        plugin.saveConfig();

        return economyManager != null;
    }

    private void loadCommands() {
        log(Level.INFO, "Initializing commands...");
        COMMANDS.forEach((name, command) -> plugin.getCommand(name).setExecutor(command));
        log(Level.INFO, "Initialized commands.");
    }

    private void loadListeners() {
        log(Level.INFO, "Initializing listeners...");
        Bukkit.getServer().getPluginManager().registerEvents(new JoinQuitListener(this), plugin);
        log(Level.INFO, "Initialized listeners.");
    }

    private void shutdown(){
        Bukkit.getServer().getPluginManager().disablePlugin(plugin);
    }

    public GithubVersionChecker getVersionChecker() { return versionChecker; }

    /**
     * Get the active EconomyManager
     * @return EconomyManager
     */
    public EconomyManager getEconomyManager() { return economyManager; }

    /**
     * Get the active TransactionLogger
     * @return TransactionLogger, if there is one.
     */
    public Optional<TransactionLogger> getTransactionLogger() { return Optional.ofNullable(transactionLogger); }

    /**
     * Get the current plugin instance.
     * @return Instance
     */
    @Deprecated
    public static SaneEconomy getInstance() { return instance; }

    /**
     * Get the logger for the plugin.
     * @return Plugin logger.
     */
    public static java.util.logging.Logger logger(){ return instance.getPlugin().getLogger(); }

    public SanePlugin getPlugin() { return plugin; }

    public VaultHook getVaultHook() { return vaultHook; }

    private void log(Level level, String message) {
        plugin.getServer().getLogger().log(level, (isBukkit) ? ChatColor.stripColor(message) : message);
    }
}
