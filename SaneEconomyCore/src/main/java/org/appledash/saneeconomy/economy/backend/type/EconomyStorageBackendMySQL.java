package org.appledash.saneeconomy.economy.backend.type;

import org.appledash.saneeconomy.SaneEconomy;
import org.appledash.saneeconomy.economy.economable.Economable;
import org.appledash.saneeconomy.utils.DatabaseCredentials;
import org.appledash.saneeconomy.utils.MySQLConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Created by AppleDash on 6/14/2016.
 * Blackjack is still best pony.
 */
public class EconomyStorageBackendMySQL extends EconomyStorageBackendCaching {
    private final MySQLConnection dbConn;

    public EconomyStorageBackendMySQL(DatabaseCredentials dbCredentials) {
        this.dbConn = new MySQLConnection(dbCredentials);
    }

    private void createTables() {
        try (Connection conn = dbConn.openConnection()) {
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

        for (Entry<String, Double> e : oldBalances.entrySet()) {
            ps = conn.prepareStatement("INSERT INTO `saneeconomy_balances` (unique_identifier, balance) VALUES (?, ?)");
            ps.setString(1, "player:" + e.getKey());
            ps.setDouble(2, e.getValue());
            ps.executeUpdate();
        }
        reloadDatabase();
        SaneEconomy.logger().info("Schema upgrade complete!");
    }

    private boolean checkTableExists(String tableName) {
        try (Connection conn = dbConn.openConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM information_schema.tables WHERE table_schema = ? AND table_name = ? LIMIT 1");
            ps.setString(1, dbConn.getCredentials().getDatabaseName());
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
        createTables();
        try (Connection conn = dbConn.openConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM `saneeconomy_balances`");
            ResultSet rs = ps.executeQuery();

            balances.clear();

            while (rs.next()) {
                balances.put(rs.getString("unique_identifier"), rs.getDouble("balance"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to reload data from SQL.", e);
        }
    }

    @Override
    public synchronized void setBalance(final Economable economable, final double newBalance) {
        final double oldBalance = getBalance(economable);
        balances.put(economable.getUniqueIdentifier(), newBalance);

        dbConn.executeAsyncOperation((conn) -> {
            try {
                ensureAccountExists(economable, conn);
                PreparedStatement statement = conn.prepareStatement("UPDATE `saneeconomy_balances` SET balance = ? WHERE `unique_identifier` = ?");
                statement.setDouble(1, newBalance);
                statement.setString(2, economable.getUniqueIdentifier());
                statement.executeUpdate();
            } catch (Exception e) {
                balances.put(economable.getUniqueIdentifier(), oldBalance);
                throw new RuntimeException("SQL error has occurred.", e);
            }
        });
    }

    private synchronized void ensureAccountExists(Economable economable, Connection conn) throws SQLException {
        if (!accountExists(economable, conn)) {
            PreparedStatement statement = conn.prepareStatement("INSERT INTO `saneeconomy_balances` (unique_identifier, balance) VALUES (?, 0.0)");
            statement.setString(1, economable.getUniqueIdentifier());
            statement.executeUpdate();
        }
    }

    private synchronized boolean accountExists(Economable economable, Connection conn) throws SQLException {
        PreparedStatement statement = conn.prepareStatement("SELECT 1 FROM `saneeconomy_balances` WHERE `unique_identifier` = ?");
        statement.setString(1, economable.getUniqueIdentifier());

        ResultSet rs = statement.executeQuery();

        return rs.next();
    }

    @Override
    public void waitUntilFlushed() {
        dbConn.waitUntilFlushed();
    }

    public MySQLConnection getConnection() {
        return dbConn;
    }
}
