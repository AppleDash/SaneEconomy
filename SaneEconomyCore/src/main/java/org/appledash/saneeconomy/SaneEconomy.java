package org.appledash.saneeconomy;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
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

    private final Map<String, SaneCommand> commands = new HashMap<String, SaneCommand>() {
        {
            this.put("balance", new BalanceCommand(SaneEconomy.this));
            this.put("ecoadmin", new EconomyAdminCommand(SaneEconomy.this));
            this.put("pay", new PayCommand(SaneEconomy.this));
            this.put("saneeconomy", new SaneEcoCommand(SaneEconomy.this));
            this.put("balancetop", new BalanceTopCommand(SaneEconomy.this));
        }
    };

    public SaneEconomy() {
        instance = this;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        if (!this.loadConfig()) { /* Invalid backend type or connection error of some sort */
            this.shutdown();
            return;
        }

        if (this.getConfig().getBoolean("locale-override", false)) {
            Locale.setDefault(Locale.ENGLISH);
        }

        this.loadCommands();
        this.loadListeners();

        if (this.getServer().getPluginManager().isPluginEnabled("Vault")) {
            this.vaultHook = new VaultHook(this);
            this.vaultHook.hook();
            this.getLogger().info("Hooked into Vault.");
        } else {
            this.getLogger().info("Not hooking into Vault because it isn't loaded.");
        }

        if (this.getConfig().getBoolean("update-check", true)) {
            this.versionChecker = new GithubVersionChecker("SaneEconomyCore", this.getDescription().getVersion().replace("-SNAPSHOT", ""));
            this.getServer().getScheduler().runTaskAsynchronously(this, this.versionChecker::checkUpdateAvailable);
        }

        this.getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            this.economyManager.getBackend().reloadTopPlayerBalances();
        }, 0L, (20L * this.getConfig().getLong("economy.baltop-update-interval", 300L)) /* Update baltop every 5 minutes by default */);

        if (this.getConfig().getBoolean("multi-server-sync", false)) {
            this.getServer().getPluginManager().registerEvents(new Listener() {
                @EventHandler
                public void onTransaction(SaneEconomyTransactionEvent evt) { // Trust me, I'm a doctor.
                    OfflinePlayer[] playersToSync = { evt.getTransaction().getSender().tryCastToPlayer(), evt.getTransaction().getReceiver().tryCastToPlayer() };

                    Player fakeSender = Iterables.getFirst(SaneEconomy.this.getServer().getOnlinePlayers(), null);

                    if (fakeSender == null) {
                        return;
                    }

                    Arrays.stream(playersToSync).filter(p -> (p != null) && !p.isOnline()).forEach(p -> {
                        ByteArrayDataOutput bado = ByteStreams.newDataOutput();
                        bado.writeUTF("Forward");
                        bado.writeUTF("ONLINE");
                        bado.writeUTF("SaneEconomy");
                        bado.writeUTF("SyncPlayer");
                        bado.writeUTF(p.getUniqueId().toString());
                        fakeSender.sendPluginMessage(SaneEconomy.this, "BungeeCord", bado.toByteArray());
                    });
                }
            }, this);
            this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", (channel, player, bytes) -> {
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
                        this.getLogger().warning("Invalid OpCode received on SaneEconomy plugin message channel: " + opCode);
                    }
                }
            });
            this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        }
    }

    @Override
    public void onDisable() {
        if (this.vaultHook != null) {
            this.getLogger().info("Unhooking from Vault.");
            this.vaultHook.unhook();
        }

        this.flushEconomyManager();
    }

    private void flushEconomyManager() {
        if (this.economyManager != null) {
            this.getLogger().info("Flushing database...");
            this.economyManager.getBackend().waitUntilFlushed();

            if (this.economyManager.getBackend() instanceof EconomyStorageBackendMySQL) {
                ((EconomyStorageBackendMySQL) this.economyManager.getBackend()).closeConnections();
                if (!((EconomyStorageBackendMySQL) this.economyManager.getBackend()).getConnection().getConnection().isFinished()) {
                    this.getLogger().warning("SaneDatabase didn't terminate all threads, something weird is going on?");
                }
            }
        }
    }

    public boolean loadConfig() {
        File configFile = new File(this.getDataFolder(), "config.yml");

        if (configFile.exists() && this.getConfig().getBoolean("debug", false)) {
            this.getLogger().info("Resetting configuration to default since debug == true.");
            configFile.delete();
            this.saveDefaultConfig();
            this.reloadConfig();
            this.getConfig().set("debug", true);
            this.saveConfig();
        } else {
            if (!configFile.exists()) {
                this.saveDefaultConfig();
            }
            this.reloadConfig();
        }

        this.flushEconomyManager(); // If we're reloading the configuration, we flush the old economy manager first

        SaneEconomyConfiguration config = new SaneEconomyConfiguration(this);

        this.economyManager = config.loadEconomyBackend();
        this.transactionLogger = config.loadLogger();

        this.saveConfig();

        return this.economyManager != null;
    }

    private void loadCommands() {
        this.getLogger().info("Initializing commands...");
        this.commands.forEach((name, command) -> this.getCommand(name).setExecutor(command));
        this.getLogger().info("Initialized commands.");
    }

    private void loadListeners() {
        this.getLogger().info("Initializing listeners...");
        this.getServer().getPluginManager().registerEvents(new JoinQuitListener(this), this);
        this.getLogger().info("Initialized listeners.");
    }

    private void shutdown() {
        this.getServer().getPluginManager().disablePlugin(this);
    }

    public GithubVersionChecker getVersionChecker() {
        return this.versionChecker;
    }

    /**
     * Get the active EconomyManager
     * @return EconomyManager
     */
    @Override
    public EconomyManager getEconomyManager() {
        return this.economyManager;
    }

    /**
     * Get the active TransactionLogger
     * @return TransactionLogger, if there is one.
     */
    @Override
    public Optional<TransactionLogger> getTransactionLogger() {
        return Optional.ofNullable(this.transactionLogger);
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
    public static Logger logger() {
        return instance.getLogger();
    }

    @Override
    public VaultHook getVaultHook() {
        return this.vaultHook;
    }

    @Override
    public String getLastName(UUID uuid) {
        return this.economyManager.getBackend().getLastName("player:" + uuid.toString());
    }
}
