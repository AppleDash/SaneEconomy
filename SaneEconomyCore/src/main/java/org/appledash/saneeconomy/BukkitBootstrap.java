package org.appledash.saneeconomy;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Optional;
import java.util.logging.Level;
import me.lucko.jarrelocator.Relocation;
import org.appledash.saneeconomy.economy.EconomyManager;
import org.appledash.saneeconomy.economy.logger.TransactionLogger;
import org.appledash.saneeconomy.utils.JarUtil;
import org.appledash.saneeconomy.utils.LogUtil;
import org.appledash.saneeconomy.vault.VaultHook;
import org.appledash.sanelib.SanePlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BukkitBootstrap extends SanePlugin implements ISaneEconomy {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private SaneEconomy concrete;
    private final String shadedPath = "org{}appledash{}saneeconomy{}shaded";
    private final boolean isBukkit;

    public BukkitBootstrap() {
        super();
        isBukkit = Bukkit.getName().equals("Bukkit") || Bukkit.getName().equals("CraftBukkit");
    }

    public SaneEconomy getConcrete() { return concrete; }

    @Override
    public void onLoad() {
        try {
            loadJars(new File(getDataFolder(), "external"), (URLClassLoader) getClass().getClassLoader());
        } catch (ClassCastException | IOException | IllegalAccessException | InvocationTargetException ex) {
            logger.error(ex.getMessage(), ex);
            throw new RuntimeException("Could not load required deps.");
        }

        concrete = new SaneEconomy(this);

        concrete.onLoad();
    }

    @Override
    public void onEnable() {
        concrete.onEnable();
    }

    @Override
    public void onDisable() {
        concrete.onDisable();
    }

    private void loadJars(File jarsFolder, URLClassLoader classLoader) throws IOException, IllegalAccessException, InvocationTargetException {
        if (jarsFolder.exists() && !jarsFolder.isDirectory()) {
            Files.delete(jarsFolder.toPath());
        }
        if (!jarsFolder.exists()) {
            if (!jarsFolder.mkdirs()) {
                throw new IOException("Could not create parent directory structure.");
            }
        }

        log(Level.INFO, LogUtil.getHeading() + ChatColor.YELLOW + "Loading dep " + ChatColor.WHITE + "HikariCP");
        JarUtil.loadJar("http://central.maven.org/maven2/com/zaxxer/HikariCP/3.2.0/HikariCP-3.2.0.jar",
                new File(jarsFolder, "HikariCP-3.2.0.jar"),
                new File(jarsFolder, "HikariCP-3.2.0-relocated.jar"),
                classLoader,
                Collections.singletonList(new Relocation(parse("com{}zaxxer{}hikari"), parse(shadedPath + "{}com{}zaxxer{}hikari"))));

        try {
            Class.forName("org.sqlite.JDBC", false, classLoader);
        } catch (ClassNotFoundException ignored) {
            log(Level.INFO, LogUtil.getHeading() + ChatColor.YELLOW + "Loading dep " + ChatColor.WHITE + "SQLite");
            JarUtil.loadJar("http://central.maven.org/maven2/org/xerial/sqlite-jdbc/3.25.2/sqlite-jdbc-3.25.2.jar",
                    new File(jarsFolder, "sqlite-jdbc-3.25.2.jar"),
                    classLoader);
        }

        try {
            DriverManager.getDriver("org.sqlite.JDBC");
        } catch (SQLException ignored) {
            try {
                DriverManager.registerDriver((Driver) Class.forName("org.sqlite.JDBC", true, classLoader).newInstance());
            } catch (ClassNotFoundException | InstantiationException | SQLException ex) {
                logger.error(ex.getMessage(), ex);
            }
        }

        try {
            Class.forName("com.mysql.jdbc.Driver", false, classLoader);
        } catch (ClassNotFoundException ignored) {
            log(Level.INFO, LogUtil.getHeading() + ChatColor.YELLOW + "Loading dep " + ChatColor.WHITE + "MySQL");
            JarUtil.loadJar("http://central.maven.org/maven2/mysql/mysql-connector-java/8.0.13/mysql-connector-java-8.0.13.jar",
                    new File(jarsFolder, "mysql-connector-java-8.0.13.jar"),
                    classLoader);
        }

        try {
            DriverManager.getDriver("com.mysql.jdbc.Driver");
        } catch (SQLException ignored) {
            try {
                DriverManager.registerDriver((Driver) Class.forName("com.mysql.jdbc.Driver", true, classLoader).newInstance());
            } catch (ClassNotFoundException | InstantiationException | SQLException ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
    }

    // Because Maven's relocate is maybe sometimes a bit too powerful ;)
    private String parse(String input) {
        return input.replace("{}", ".");
    }

    private void log(Level level, String message) {
        getServer().getLogger().log(level, (isBukkit) ? ChatColor.stripColor(message) : message);
    }

    public EconomyManager getEconomyManager() {
        return concrete.getEconomyManager();
    }

    public Optional<TransactionLogger> getTransactionLogger() {
        return concrete.getTransactionLogger();
    }

    public VaultHook getVaultHook() {
        return concrete.getVaultHook();
    }
}
