package org.appledash.saneeconomy.economy.backend.type;

import org.appledash.saneeconomy.economy.economable.Economable;
import org.appledash.saneeconomy.utils.database.MySQLConnection;
import org.appledash.sanelib.database.DatabaseCredentials;

import java.math.BigDecimal;
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
    private static final String SANEECONOMY_BALANCES = "saneeconomy_balances";
    private static final String SANEECONOMY_SCHEMA = "saneeconomy_schema";

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

            if (!checkTableExists(dbConn.getTable(SANEECONOMY_SCHEMA))) {
                schemaVersion = -1;
            } else {
                PreparedStatement ps = conn.prepareStatement(String.format("SELECT `val` FROM `%s` WHERE `key` = 'schema_version'", dbConn.getTable(SANEECONOMY_SCHEMA)));
                ResultSet rs = ps.executeQuery();

                if (!rs.next()) {
                    throw new RuntimeException("Invalid database schema!");
                }

                schemaVersion = Integer.parseInt(rs.getString("val"));
            }

            if (schemaVersion == -1) {
                conn.prepareStatement(String.format("CREATE TABLE IF NOT EXISTS `%s` (`key` VARCHAR(32) PRIMARY KEY, `val` TEXT)", dbConn.getTable(SANEECONOMY_SCHEMA))).executeUpdate();
                conn.prepareStatement(String.format("REPLACE INTO %s (`key`, `val`) VALUES ('schema_version', 4)", dbConn.getTable(SANEECONOMY_SCHEMA))).executeUpdate();
                conn.prepareStatement(String.format("CREATE TABLE `%s` (unique_identifier VARCHAR(128) PRIMARY KEY, last_name VARCHAR(16), balance TEXT)", dbConn.getTable(SANEECONOMY_BALANCES))).executeUpdate();
                schemaVersion = 4;
            }

            if (schemaVersion == 2) {
                conn.prepareStatement(String.format("ALTER TABLE `%s` ADD `last_name` VARCHAR(16)", dbConn.getTable(SANEECONOMY_BALANCES))).executeUpdate();
                conn.prepareStatement(String.format("REPLACE INTO %s (`key`, `val`) VALUES ('schema_version', 3)", dbConn.getTable(SANEECONOMY_SCHEMA))).executeUpdate();

                schemaVersion = 3;
            }

            if (schemaVersion == 3) {
                conn.prepareStatement(String.format("ALTER TABLE `%s` ADD `balance_new` TEXT", dbConn.getTable(SANEECONOMY_BALANCES))).executeUpdate();
                conn.prepareStatement(String.format("UPDATE `%s` SET balance_new = balance", dbConn.getTable(SANEECONOMY_BALANCES))).executeUpdate();
                conn.prepareStatement(String.format("ALTER TABLE `%s` DROP COLUMN `balance`", dbConn.getTable(SANEECONOMY_BALANCES))).executeUpdate();
                conn.prepareStatement(String.format("ALTER TABLE `%s` CHANGE COLUMN `balance_new` `balance` TEXT", dbConn.getTable(SANEECONOMY_BALANCES))).executeUpdate();
                conn.prepareStatement(String.format("REPLACE INTO %s (`key`, `val`) VALUES ('schema_version', 4)", dbConn.getTable(SANEECONOMY_SCHEMA))).executeUpdate();

                schemaVersion = 4;
            }

            if (schemaVersion != 4) {
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
            PreparedStatement ps = dbConn.prepareStatement(conn, String.format("SELECT * FROM `%s`", dbConn.getTable(SANEECONOMY_BALANCES)));
            ResultSet rs = ps.executeQuery();

            balances.clear();

            while (rs.next()) {
                balances.put(rs.getString("unique_identifier"), new BigDecimal(rs.getString("balance")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to reload data from SQL.", e);
        }
    }

    @Override
    public void setBalance(final Economable economable, final BigDecimal newBalance) {
        final BigDecimal oldBalance = getBalance(economable);
        balances.put(economable.getUniqueIdentifier(), newBalance);

        dbConn.executeAsyncOperation("set_balance_" + economable.getUniqueIdentifier(), (conn) -> {
            try {
                ensureAccountExists(economable, conn);
                conn.prepareStatement("LOCK TABLE " + dbConn.getTable(SANEECONOMY_BALANCES) + " WRITE").execute();
                PreparedStatement statement = dbConn.prepareStatement(conn, String.format("UPDATE `%s` SET balance = ?, last_name = ? WHERE `unique_identifier` = ?", dbConn.getTable(SANEECONOMY_BALANCES)));
                statement.setString(1, newBalance.toString());
                statement.setString(2, economable.getName());
                statement.setString(3, economable.getUniqueIdentifier());
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
            PreparedStatement statement = dbConn.prepareStatement(conn, String.format("INSERT INTO `%s` (unique_identifier, last_name, balance) VALUES (?, ?, 0.0)", dbConn.getTable(SANEECONOMY_BALANCES)));
            statement.setString(1, economable.getUniqueIdentifier());
            statement.setString(2, economable.getName());
            statement.executeUpdate();
        }
    }

    private boolean accountExists(Economable economable, Connection conn) throws SQLException {
        PreparedStatement statement = dbConn.prepareStatement(conn, String.format("SELECT 1 FROM `%s` WHERE `unique_identifier` = ?", dbConn.getTable(SANEECONOMY_BALANCES)));
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


    @Override
    public void reloadEconomable(String uniqueIdentifier, EconomableReloadReason reason) {
        dbConn.executeAsyncOperation("reload_economable_" + uniqueIdentifier, (conn) -> {
            try {
                PreparedStatement ps = conn.prepareStatement(String.format("SELECT balance FROM `%s` WHERE `unique_identifier` = ?", dbConn.getTable(SANEECONOMY_BALANCES)));
                ps.setString(1, uniqueIdentifier);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    this.balances.put(uniqueIdentifier, new BigDecimal(rs.getString("balance")));
                }
            } catch (SQLException e) {
                throw new RuntimeException("SQL error has occured", e);
            }
        });
    }
}
