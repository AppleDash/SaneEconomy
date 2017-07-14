package org.appledash.saneeconomy.economy.backend.type;

import org.appledash.saneeconomy.economy.economable.Economable;
import org.appledash.saneeconomy.utils.database.MySQLConnection;
import org.appledash.sanelib.database.DatabaseCredentials;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by AppleDash on 6/14/2016.
 * Blackjack is still best pony.
 */
public class EconomyStorageBackendMySQL extends EconomyStorageBackendCaching {
    private static final Logger LOGGER = Logger.getLogger("EconomyStorageBackendMySQL");
    static {
        LOGGER.setLevel(Level.FINEST);
    }
    private final MySQLConnection dbConn;

    public EconomyStorageBackendMySQL(DatabaseCredentials dbCredentials) {
        this.dbConn = new MySQLConnection(dbCredentials);
    }

    private void createTables() {
        try (Connection conn = dbConn.openConnection()) {
            int schemaVersion;

            if (!checkTableExists(dbConn.getTable("saneeconomy_schema"))) {
                schemaVersion = -1;
            } else {
                PreparedStatement ps = conn.prepareStatement(String.format("SELECT `val` FROM `%s` WHERE `key` = 'schema_version'", dbConn.getTable("saneeconomy_schema")));
                ResultSet rs = ps.executeQuery();

                if (!rs.next()) {
                    throw new RuntimeException("Invalid database schema!");
                }

                schemaVersion = Integer.valueOf(rs.getString("val"));
            }

            if (schemaVersion == -1) {
                conn.prepareStatement(String.format("CREATE TABLE IF NOT EXISTS `%s` (`key` VARCHAR(32) PRIMARY KEY, `val` TEXT)", dbConn.getTable("saneeconomy_schema"))).executeUpdate();
                conn.prepareStatement(String.format("REPLACE INTO %s (`key`, `val`) VALUES ('schema_version', 2)", dbConn.getTable("saneeconomy_schema"))).executeUpdate();
                conn.prepareStatement(String.format("CREATE TABLE `%s` (unique_identifier VARCHAR(128) PRIMARY KEY, balance DECIMAL(18, 2))", dbConn.getTable("saneeconomy_balances"))).executeUpdate();
                schemaVersion = 2;
            }

            if (schemaVersion != 2) {
                throw new RuntimeException("Invalid database schema version!");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create tables!", e);
        }
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
    public synchronized void reloadDatabase() {
        waitUntilFlushed();
        createTables();
        try (Connection conn = dbConn.openConnection()) {
            PreparedStatement ps = dbConn.prepareStatement(conn, String.format("SELECT * FROM `%s`", dbConn.getTable("saneeconomy_balances")));
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
    public void setBalance(final Economable economable, final double newBalance) {
        final double oldBalance = getBalance(economable);
        balances.put(economable.getUniqueIdentifier(), newBalance);

        dbConn.executeAsyncOperation("set_balance_" + economable.getUniqueIdentifier(), (conn) -> {
            try {
                ensureAccountExists(economable, conn);
                conn.prepareStatement("LOCK TABLE " + dbConn.getTable("saneeconomy_balances") + " WRITE").execute();
                PreparedStatement statement = dbConn.prepareStatement(conn, String.format("UPDATE `%s` SET balance = ? WHERE `unique_identifier` = ?", dbConn.getTable("saneeconomy_balances")));
                statement.setDouble(1, newBalance);
                statement.setString(2, economable.getUniqueIdentifier());
                statement.executeUpdate();
                conn.prepareStatement("UNLOCK TABLES").execute();
            } catch (Exception e) {
                balances.put(economable.getUniqueIdentifier(), oldBalance);
                throw new RuntimeException("SQL error has occurred.", e);
            }
        });
    }

    private void ensureAccountExists(Economable economable, Connection conn) throws SQLException {
        if (!accountExists(economable, conn)) {
            PreparedStatement statement = dbConn.prepareStatement(conn, String.format("INSERT INTO `%s` (unique_identifier, balance) VALUES (?, 0.0)", dbConn.getTable("saneeconomy_balances")));
            statement.setString(1, economable.getUniqueIdentifier());
            statement.executeUpdate();
        }
    }

    private boolean accountExists(Economable economable, Connection conn) throws SQLException {
        PreparedStatement statement = dbConn.prepareStatement(conn, String.format("SELECT 1 FROM `%s` WHERE `unique_identifier` = ?", dbConn.getTable("saneeconomy_balances")));
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

    public void closeConnections() {
        this.dbConn.getConnection().cleanup();
    }
}
