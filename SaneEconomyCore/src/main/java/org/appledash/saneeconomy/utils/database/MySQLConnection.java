package org.appledash.saneeconomy.utils.database;

import org.appledash.saneeconomy.SaneEconomy;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Created by appledash on 9/19/16.
 * Blackjack is best pony.
 */
public class MySQLConnection {
    private static final Logger LOGGER = Logger.getLogger("MySQLConnection");
    private static final int MAX_OPEN_TRANSACTIONS = 5;
    private final DatabaseCredentials dbCredentials;
    private final AtomicInteger openTransactions = new AtomicInteger(0);

    public MySQLConnection(DatabaseCredentials dbCredentials) {
        this.dbCredentials = dbCredentials;

        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("No MySQL driver found.", e);
        }
    }

    public Connection openConnection() {
        try {
            return DriverManager.getConnection(dbCredentials.getJDBCURL(), dbCredentials.getUsername(), dbCredentials.getPassword());
        } catch (SQLException e) {
            throw new RuntimeException("Database unavailable.", e);
        }
    }

    public PreparedStatement prepareStatement(Connection conn, String sql) throws SQLException {
        PreparedStatement preparedStatement = conn.prepareStatement(sql);

        preparedStatement.setQueryTimeout(dbCredentials.getQueryTimeout()); // 5 second timeout

        return preparedStatement;
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return prepareStatement(openConnection(), sql);
    }

    public boolean testConnection() {
        try (Connection ignored = openConnection()) {
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void executeAsyncOperation(Consumer<Connection> callback) {
        Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask(SaneEconomy.getInstance(), () -> {
            doExecuteAsyncOperation(1, callback);
        });
    }

    // This is a bit weird because it has to account for recursion...
    private void doExecuteAsyncOperation(int levels, Consumer<Connection> callback) {
        if (levels == 1) { // First level
            waitForSlot();
            openTransactions.incrementAndGet();
        }

        try (Connection conn = openConnection()) {
            callback.accept(conn);
        } catch (Exception e) {
            if (levels > dbCredentials.getMaxRetries()) {
                throw new RuntimeException("This shouldn't happen (database error)", e);
            }

            LOGGER.severe("An internal SQL error has occured, trying up to " + (5 - levels) + " more times...");
            e.printStackTrace();
            levels++;
            doExecuteAsyncOperation(levels, callback);
        } finally {
            if (levels == 1) { // The recursion is done, we may have thrown an exception, maybe not - but either way we need to mark the transaction as closed.
                openTransactions.decrementAndGet();
            }
        }
    }

    public DatabaseCredentials getCredentials() {
        return dbCredentials;
    }

    public String getTable(String tableName) {
        return dbCredentials.getTablePrefix() + tableName;
    }

    private void waitForSlot() {
        while (openTransactions.get() >= MAX_OPEN_TRANSACTIONS) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {

            }
        }
    }

    public void waitUntilFlushed() {
        while (openTransactions.get() > 0) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {

            }
        }
    }
}
