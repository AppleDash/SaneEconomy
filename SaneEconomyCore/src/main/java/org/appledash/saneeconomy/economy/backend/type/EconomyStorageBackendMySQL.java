package org.appledash.saneeconomy.economy.backend.type;

import org.appledash.saneeconomy.SaneEconomy;
import org.appledash.saneeconomy.economy.economable.Economable;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by AppleDash on 6/14/2016.
 * Blackjack is still best pony.
 */
public class EconomyStorageBackendMySQL extends EconomyStorageBackendCaching {
    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;

    public EconomyStorageBackendMySQL(String dbUrl, String dbUser, String dbPassword) {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;

        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("No MySQL driver found.");
        }
    }

    private Connection openConnection() {
        try {
            return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        } catch (SQLException e) {
            throw new RuntimeException("Database unavailable.");
        }
    }

    public boolean testConnection() {
        try {
            openConnection().close();
            createTables();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void createTables() {
        Connection conn = openConnection();
        try {
            int schemaVersion;
            if (!checkTableExists("saneeconomy_schema")) {
                if (checkTableExists("player_balances")) {
                    schemaVersion = 1;
                } else {
                    schemaVersion = 0;
                }
            } else {
                PreparedStatement ps = conn.prepareStatement("SELECT `val` FROM saneeconomy_schema WHERE `key` = 'schema_version'");
                ps.executeQuery();
                ResultSet rs = ps.getResultSet();

                if (!rs.next()) {
                    throw new RuntimeException("Invalid database schema!");
                }

                schemaVersion = Integer.valueOf(rs.getString("val"));
            }

            if (schemaVersion < 2) {
                if (schemaVersion < 1) {
                    PreparedStatement ps = conn.prepareStatement("CREATE TABLE IF NOT EXISTS `player_balances` (player_uuid CHAR(36), balance DECIMAL(18, 2))");
                    ps.executeUpdate();
                }
                conn.prepareStatement("CREATE TABLE IF NOT EXISTS `saneeconomy_schema` (`key` VARCHAR(32) PRIMARY KEY, `val` TEXT)").executeUpdate();
                upgradeSchema1To2(conn);
            }

            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create tables!", e);
        }
    }

    private void upgradeSchema1To2(Connection conn) throws SQLException {
        SaneEconomy.logger().info("Upgrading database schema from version 1 to version 2. This might take a little while...");
        PreparedStatement ps = conn.prepareStatement("REPLACE INTO `saneeconomy_schema` (`key`, `val`) VALUES ('schema_version', '2')");
        ps.executeUpdate();
        conn.prepareStatement("CREATE TABLE `saneeconomy_balances` (unique_identifier VARCHAR(128) PRIMARY KEY, balance DECIMAL(18, 2))").executeUpdate();
        ps = conn.prepareStatement("SELECT * FROM `player_balances`");
        ResultSet rs = ps.executeQuery();

        Map<String, Double> oldBalances = new HashMap<>();

        while (rs.next()) {
            oldBalances.put(rs.getString("player_uuid"), rs.getDouble("balance"));
        }

        for (Map.Entry<String, Double> e : oldBalances.entrySet()) {
            ps = conn.prepareStatement("INSERT INTO `saneeconomy_balances` (unique_identifier, balance) VALUES (?, ?)");
            ps.setString(1, "player:" + e.getKey());
            ps.setDouble(2, e.getValue());
            ps.executeUpdate();
        }
        reloadDatabase();
        SaneEconomy.logger().info("Schema upgrade complete!");
    }

    private boolean checkTableExists(String tableName) {
        Connection conn = openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM information_schema.tables WHERE table_schema = ? AND table_name = ? LIMIT 1");
            ps.setString(1, dbUrl.substring("jdbc:mysql://".length()).split("/")[1]); // FIXME: There has to be a better way.
            ps.setString(2, tableName);
            ps.executeQuery();
            ResultSet rs = ps.getResultSet();

            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check if table exists!", e);
        }
    }

    @Override
    public void reloadDatabase() {
        Connection conn = openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM `saneeconomy_balances`");
            ResultSet rs = ps.executeQuery();

            balances.clear();

            while (rs.next()) {
                balances.put(rs.getString("unique_identifier"), rs.getDouble("balance"));
            }

            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to reload data from SQL.", e);
        }
    }

    @Override
    public synchronized void setBalance(final Economable economable, final double newBalance) {
        final double oldBalance = getBalance(economable);
        balances.put(economable.getUniqueIdentifier(), newBalance);

        Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask(SaneEconomy.getInstance(), () -> {
            Connection conn = openConnection();
            ensureAccountExists(economable, conn);
            try {
                PreparedStatement statement = conn.prepareStatement("UPDATE `saneeconomy_balances` SET balance = ? WHERE `unique_identifier` = ?");
                statement.setDouble(1, newBalance);
                statement.setString(2, economable.getUniqueIdentifier());
                statement.executeUpdate();
                conn.close();
            } catch (SQLException e) {
                /* Roll it back */
                balances.put(economable.getUniqueIdentifier(), oldBalance);
                throw new RuntimeException("SQL error has occurred.", e);
            }
        });
    }

    private synchronized void ensureAccountExists(Economable economable, Connection conn) {
        if (!accountExists(economable, conn)) {
            try {
                PreparedStatement statement = conn.prepareStatement("INSERT INTO `saneeconomy_balances` (unique_identifier, balance) VALUES (?, 0.0)");
                statement.setString(1, economable.getUniqueIdentifier());
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("SQL error has occurred.", e);
            }
        }
    }

    private synchronized boolean accountExists(Economable economable, Connection conn) {
        try {
            PreparedStatement statement = conn.prepareStatement("SELECT COUNT(*) FROM `saneeconomy_balances` WHERE `unique_identifier` = ?");
            statement.setString(1, economable.getUniqueIdentifier());

            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            throw new RuntimeException("SQL error has occurred.", e);
        }

        return false;
    }

    strictfp enum En {

    }
}
