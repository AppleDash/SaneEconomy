package org.appledash.saneeconomy.utils.database;

import org.appledash.sanelib.database.DatabaseCredentials;
import org.appledash.sanelib.database.SaneDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Created by appledash on 9/19/16.
 * Blackjack is best pony.
 */
public class MySQLConnection {
    private static final Logger LOGGER = Logger.getLogger("MySQLConnection");
    private final DatabaseCredentials dbCredentials;
    private final SaneDatabase saneDatabase;

    public MySQLConnection(DatabaseCredentials dbCredentials) {
        this.dbCredentials = dbCredentials;
        this.saneDatabase = new SaneDatabase(dbCredentials);
    }

    public Connection openConnection() {
        try {
            return this.saneDatabase.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Database unavailable.", e);
        }
    }

    public PreparedStatement prepareStatement(Connection conn, String sql) throws SQLException {
        PreparedStatement preparedStatement = conn.prepareStatement(sql);

        preparedStatement.setQueryTimeout(dbCredentials.getQueryTimeout()); // 5 second timeout

        return preparedStatement;
    }

    public boolean testConnection() {
        try (Connection ignored = openConnection()) {
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void executeAsyncOperation(String tag, Consumer<Connection> callback) {
        this.saneDatabase.runDatabaseOperationAsync(tag, () -> doExecuteAsyncOperation(1, callback));
    }

    // This is a bit weird because it has to account for recursion...
    private void doExecuteAsyncOperation(int levels, Consumer<Connection> callback) {
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
        }
    }

    public DatabaseCredentials getCredentials() {
        return dbCredentials;
    }

    public String getTable(String tableName) {
        return dbCredentials.getTablePrefix() + tableName;
    }

    public void waitUntilFlushed() {
        long startTime = System.currentTimeMillis();
        while (!this.saneDatabase.isFinished()) {
            if ((System.currentTimeMillis() - startTime) > 5000) {
                LOGGER.warning("Took too long to flush all transactions - something has probably hung :(");
                break;
            }

            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {
            }
        }
    }

    public SaneDatabase getConnection() {
        return this.saneDatabase;
    }
}
