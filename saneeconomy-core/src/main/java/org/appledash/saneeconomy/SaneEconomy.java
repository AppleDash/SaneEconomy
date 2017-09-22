package org.appledash.saneeconomy;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.appledash.saneeconomy.command.*;
import org.appledash.saneeconomy.economy.EconomyManager;
import org.appledash.saneeconomy.economy.backend.type.EconomyStorageBackendMySQL;
import org.appledash.saneeconomy.economy.logger.TransactionLogger;
import org.appledash.saneeconomy.event.SaneEconomyTransactionEvent;
import org.appledash.saneeconomy.listeners.JoinQuitListener;
import org.appledash.saneeconomy.updates.GithubVersionChecker;
import org.appledash.saneeconomy.utils.SaneEconomyConfiguration;
import org.appledash.saneeconomy.vault.VaultHook;
import org.appledash.sanelib.SanePlugin;
import org.appledash.sanelib.command.SaneCommand;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by AppleDash on 6/13/2016.
 * Blackjack is still best pony.
 */
public class SaneEconomy extends SanePlugin implements ISaneEconomy {
    private static SaneEconomy instance;
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

    public SaneEconomy() {
        instance = this;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        if (!loadConfig()) { /* Invalid backend type or connection error of some sort */
            shutdown();
            return;
        }

        if (this.getConfig().getBoolean("locale-override", false)) {
            Locale.setDefault(Locale.ENGLISH);
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

        if(getConfig().getBoolean("update-check", true)) {
            versionChecker = new GithubVersionChecker("SaneEconomyCore", this.getDescription().getVersion());
            getServer().getScheduler().runTaskAsynchronously(this, versionChecker::checkUpdateAvailable);
        }

        // Update baltop every 5 minutes
        int baltopReloadInterval = getConfig().getInt("baltop.reload-interval", 300);
        getServer().getScheduler().runTaskTimerAsynchronously(this,
                () -> economyManager.getBackend().reloadTopPlayerBalances(), 0, (20 * baltopReloadInterval));

        if (this.getConfig().getBoolean("bungeecord.enable-sync", false)) {
            this.getServer().getPluginManager().registerEvents(new Listener() {
                @EventHandler
                public void onTransaction(SaneEconomyTransactionEvent event) { // Trust me, I'm a doctor.
                    OfflinePlayer player = event.getTransaction().getReceiver().tryCastToPlayer();
                    OfflinePlayer sender = event.getTransaction().getSender().tryCastToPlayer();

                    if(player == null) {
                        return;
                    }

                    Set<OfflinePlayer> playersToSync = new HashSet<>();
                    playersToSync.add(player);
                    if(sender != null) {
                        playersToSync.add(sender);
                    }

                    Player fakeSender = Iterables.getFirst(getServer().getOnlinePlayers(), null);
                    if(fakeSender == null) {
                        return;
                    }

                    for(OfflinePlayer currentPlayer : playersToSync) {
                        if (currentPlayer.isOnline()) {
                            continue;
                        }

                        ByteArrayDataOutput bout = ByteStreams.newDataOutput();
                        bout.writeUTF("Forward");
                        bout.writeUTF("ALL");
                        bout.writeUTF("SaneEconomy");
                        bout.writeUTF(getConfig().getString("bungeecord.section-name", "global"));
                        bout.writeUTF("SyncUser");
                        bout.writeUTF(currentPlayer.getUniqueId().toString());
                        fakeSender.sendPluginMessage(SaneEconomy.this, "BungeeCord", bout.toByteArray());
                    }
                }
            }, this);

            this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", (channel, player, bytes) -> {
                if (!channel.equals("BungeeCord")) {
                    return;
                }

                ByteArrayDataInput bin = ByteStreams.newDataInput(bytes);

                String subChannel = bin.readUTF();
                if (!subChannel.equals("SaneEconomy")) {
                    return;
                }

                String sectionName = bin.readUTF();
                if (!sectionName.equals(getConfig().getString("bungeecord.section-name", "global"))) {
                    return;
                }

                String commandName = bin.readUTF();
                if (commandName.equals("SyncDatabase")) {
                    String uniqueId = bin.readUTF();
                    SaneEconomy.this.getEconomyManager().getBackend().reloadUser(uniqueId);
                } else {
                    SaneEconomy.this.getLogger().warning("Invalid command received on SaneEconomy plugin message channel: " + commandName + " section:" + sectionName);
                }
            });
            this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        }
    }

    @Override
    public void onDisable() {
        if (vaultHook != null) {
            getLogger().info("Unhooking from Vault.");
            vaultHook.unhook();
        }

        this.flushEconomyManager();
    }

    private void flushEconomyManager() {
        if (this.economyManager != null) {
            this.getLogger().info("Flushing database...");
            this.economyManager.getBackend().waitUntilFlushed();

            if (this.economyManager.getBackend() instanceof EconomyStorageBackendMySQL) {
                ((EconomyStorageBackendMySQL) economyManager.getBackend()).closeConnections();
                if (!((EconomyStorageBackendMySQL) economyManager.getBackend()).getConnection().getConnection().isFinished()) {
                    this.getLogger().warning("SaneDatabase didn't terminate all threads, something weird is going on?");
                }
            }
        }
    }

    public boolean loadConfig() {
        File configFile = new File(getDataFolder(), "config.yml");

        if (configFile.exists() && getConfig().getBoolean("debug", false)) {
            getLogger().info("Resetting configuration to default since debug == true.");
            configFile.delete();
            saveDefaultConfig();
            reloadConfig();
            getConfig().set("debug", true);
            saveConfig();
        } else {
            if (!configFile.exists()) {
                this.saveDefaultConfig();
            }
            this.reloadConfig();
        }

        this.flushEconomyManager(); // If we're reloading the configuration, we flush the old economy manager first

        SaneEconomyConfiguration config = new SaneEconomyConfiguration(this);

        economyManager = config.loadEconomyBackend();
        transactionLogger = config.loadLogger();

        saveConfig();

        return economyManager != null;
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

    public GithubVersionChecker getVersionChecker() {
        return versionChecker;
    }

    /**
     * Get the active EconomyManager
     * @return EconomyManager
     */
    @Override
    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    /**
     * Get the active TransactionLogger
     * @return TransactionLogger, if there is one.
     */
    @Override
    public Optional<TransactionLogger> getTransactionLogger() {
        return Optional.ofNullable(transactionLogger);
    }

    /**
     * Get the current plugin instance.
     * @return Instance
     */
    @Deprecated
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

    public VaultHook getVaultHook() {
        return vaultHook;
    }
}
