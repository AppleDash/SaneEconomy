package org.appledash.saneeconomy.utils;

import com.google.common.base.Strings;
import org.appledash.saneeconomy.ISaneEconomy;
import org.appledash.saneeconomy.SaneEconomy;
import org.appledash.saneeconomy.economy.Currency;
import org.appledash.saneeconomy.economy.EconomyManager;
import org.appledash.saneeconomy.economy.backend.EconomyStorageBackend;
import org.appledash.saneeconomy.economy.backend.type.EconomyStorageBackendFlatfile;
import org.appledash.saneeconomy.economy.backend.type.EconomyStorageBackendMySQL;
import org.appledash.saneeconomy.economy.economable.EconomableGeneric;
import org.appledash.saneeconomy.economy.logger.TransactionLogger;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.logging.Logger;

/**
 * Created by appledash on 9/18/16.
 * Blackjack is best pony.
 */
public class SaneEconomyConfiguration {
    private Logger logger;
    private SaneEconomy saneEconomy;
    private Configuration rootConfig;

    public SaneEconomyConfiguration(SaneEconomy saneEconomy) {
        this.saneEconomy = saneEconomy;
        this.rootConfig = saneEconomy.getConfig();
        this.logger = saneEconomy.getLogger();
    }

    public EconomyManager loadEconomyBackend() {
        logger.info("Initializing currency...");
        Currency currency = Currency.fromConfig(rootConfig, "currency");
        logger.info("Initialized currency: " + currency.getPluralName());

        logger.info("Initializing economy storage backend...");

        EconomyStorageBackend backend = loadBackend(rootConfig.getConfigurationSection("backend"));

        if (backend == null) {
            logger.severe("Failed to load backend!");
            return null;
        }

        logger.info("Performing initial data load...");
        backend.reloadDatabase();
        logger.info("Data loaded!");

        if (!Strings.isNullOrEmpty(rootConfig.getString("old-backend.type", null))) {
            logger.info("Old backend detected, converting... (This may take a minute or two.)");
            EconomyStorageBackend oldBackend = loadBackend(rootConfig.getConfigurationSection("old-backend"));
            if (oldBackend == null) {
                logger.severe("Failed to load old backend!");
                return null;
            }

            oldBackend.reloadDatabase();
            convertBackends(oldBackend, backend);
            logger.info("Data converted, removing old config section.");
            rootConfig.set("old-backend", null);
        }

        return new EconomyManager(saneEconomy, currency, backend);
    }

    private EconomyStorageBackend loadBackend(ConfigurationSection config) {
        EconomyStorageBackend backend;
        String backendType = config.getString("type");

        if (backendType.equalsIgnoreCase("flatfile")) {
            String backendFileName = config.getString("file", "economy.db");
            File backendFile = new File(saneEconomy.getDataFolder(), backendFileName);
            backend = new EconomyStorageBackendFlatfile(backendFile);
            logger.info("Initialized flatfile backend with file " + backendFile.getAbsolutePath());
        } else if (backendType.equalsIgnoreCase("mysql")) {
            EconomyStorageBackendMySQL mySQLBackend = new EconomyStorageBackendMySQL(loadCredentials(config));

            backend = mySQLBackend;

            logger.info("Initialized MySQL backend.");
            logger.info("Testing connection...");
            if (!mySQLBackend.testConnection()) {
                logger.severe("MySQL connection failed - cannot continue!");
                return null;
            }

            logger.info("Connection successful!");
        } else {
            logger.severe("Unknown storage backend " + backendType + "!");
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
}
