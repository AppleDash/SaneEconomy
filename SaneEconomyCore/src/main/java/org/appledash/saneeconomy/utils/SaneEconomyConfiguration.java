package org.appledash.saneeconomy.utils;

import com.google.common.base.Strings;
import org.appledash.saneeconomy.SaneEconomy;
import org.appledash.saneeconomy.economy.Currency;
import org.appledash.saneeconomy.economy.EconomyManager;
import org.appledash.saneeconomy.economy.backend.EconomyStorageBackend;
import org.appledash.saneeconomy.economy.backend.type.EconomyStorageBackendJSON;
import org.appledash.saneeconomy.economy.backend.type.EconomyStorageBackendMySQL;
import org.appledash.saneeconomy.economy.economable.EconomableGeneric;
import org.appledash.saneeconomy.economy.logger.TransactionLogger;
import org.appledash.saneeconomy.economy.logger.TransactionLoggerMySQL;
import org.appledash.sanelib.database.DatabaseCredentials;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.logging.Logger;

/**
 * Created by appledash on 9/18/16.
 * Blackjack is best pony.
 */
public class SaneEconomyConfiguration {
    private final Logger logger;
    private final SaneEconomy saneEconomy;
    private final Configuration rootConfig;

    public SaneEconomyConfiguration(SaneEconomy saneEconomy) {
        this.saneEconomy = saneEconomy;
        this.rootConfig = saneEconomy.getConfig();
        this.logger = saneEconomy.getLogger();
    }

    public EconomyManager loadEconomyBackend() {
        this.logger.info("Initializing currency...");
        Currency currency = Currency.fromConfig(this.rootConfig.getConfigurationSection("currency"));
        this.logger.info("Initialized currency: " + currency.getPluralName());

        this.logger.info("Initializing economy storage backend...");

        EconomyStorageBackend backend = this.loadBackend(this.rootConfig.getConfigurationSection("backend"));

        if (backend == null) {
            this.logger.severe("Failed to load backend!");
            return null;
        }

        this.logger.info("Performing initial data load...");
        backend.reloadDatabase();
        this.logger.info("Data loaded!");

        if (!Strings.isNullOrEmpty(this.rootConfig.getString("old-backend.type", null))) {
            this.logger.info("Old backend detected, converting... (This may take a minute or two.)");
            EconomyStorageBackend oldBackend = this.loadBackend(this.rootConfig.getConfigurationSection("old-backend"));
            if (oldBackend == null) {
                this.logger.severe("Failed to load old backend!");
                return null;
            }

            oldBackend.reloadDatabase();
            this.convertBackends(oldBackend, backend);
            this.logger.info("Data converted, removing old config section.");
            this.rootConfig.set("old-backend", null);
        }

        return new EconomyManager(this.saneEconomy, currency, backend, this.rootConfig.getString("economy.server-account", null));
    }

    /**
     * Load an EconomyStorageBackend using the information in the given ConfigurationSection.
     * @param config ConfigurationSection to read connection parameters from
     * @return Constructed EconomyStorageBackend, or null if something inappropriate happened.
     */
    private EconomyStorageBackend loadBackend(ConfigurationSection config) {
        EconomyStorageBackend backend;
        String backendType = config.getString("type");

        if (backendType.equalsIgnoreCase("json")) {
            String backendFileName = config.getString("file", "economy.json");
            File backendFile = new File(this.saneEconomy.getDataFolder(), backendFileName);
            backend = new EconomyStorageBackendJSON(backendFile);
            this.logger.info("Initialized JSON backend with file " + backendFile.getAbsolutePath());
        } else if (backendType.equalsIgnoreCase("mysql")) {
            EconomyStorageBackendMySQL mySQLBackend = new EconomyStorageBackendMySQL(this.loadCredentials(config));

            backend = mySQLBackend;

            this.logger.info("Initialized MySQL backend.");
            this.logger.info("Testing connection...");
            if (!mySQLBackend.getConnection().testConnection()) {
                this.logger.severe("MySQL connection failed - cannot continue!");
                return null;
            }

            this.logger.info("Connection successful!");
        } else {
            this.logger.severe("Unknown storage backend " + backendType + "!");
            return null;
        }

        return backend;
    }

    /**
     * Convert one EconomyStorageBackend to another.
     * Right now, this just consists of converting all player balances. Data in the old backend is kept.
     * Why is this in here?
     * @param oldBackend Old backend
     * @param newBackend New backend
     */
    private void convertBackends(EconomyStorageBackend oldBackend, EconomyStorageBackend newBackend) {
        oldBackend.getAllBalances().forEach((uniqueId, balance) ->
                newBackend.setBalance(new EconomableGeneric(uniqueId), balance)
        );

        newBackend.waitUntilFlushed();
    }

    public TransactionLogger loadLogger() {
        if (!this.rootConfig.getBoolean("log-transactions", false)) {
            return null;
        }

        this.logger.info("Attempting to load transaction logger...");

        if (this.rootConfig.getConfigurationSection("logger-database") == null) {
            this.logger.severe("No transaction logger database defined, cannot possibly connect!");
            return null;
        }

        DatabaseCredentials credentials = this.loadCredentials(this.rootConfig.getConfigurationSection("logger-database"));

        TransactionLoggerMySQL transactionLogger = new TransactionLoggerMySQL(credentials);

        if (transactionLogger.testConnection()) {
            this.logger.info("Initialized MySQL transaction logger.");
            return transactionLogger;
        }

        this.logger.severe("Failed to connect to MySQL database for transaction logger!");
        return null;
    }

    /**
     * Load database host, port, username, password, and db name from a ConfigurationSection
     * @param config ConfigurationSection containing the right fields.
     * @return DatabaseCredentials with the information from the config.
     */
    private DatabaseCredentials loadCredentials(ConfigurationSection config) {
        String databaseType = config.getString("type", "mysql");
        String backendHost = config.getString("host");
        int backendPort = config.getInt("port", 3306);
        String backendDb = config.getString("database");
        String backendUser = config.getString("username");
        String backendPass = config.getString("password");
        String tablePrefix = config.getString("table_prefix", "");
        boolean useSsl = config.getBoolean("use_ssl", false);

        return new DatabaseCredentials(
                   databaseType, backendHost, backendPort, backendUser, backendPass, backendDb, tablePrefix, useSsl
               );
    }
}
