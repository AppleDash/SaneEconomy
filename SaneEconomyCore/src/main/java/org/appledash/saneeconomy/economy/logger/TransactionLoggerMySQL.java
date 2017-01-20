package org.appledash.saneeconomy.economy.logger;

import org.appledash.saneeconomy.economy.transaction.Transaction;
import org.appledash.saneeconomy.economy.transaction.TransactionReason;
import org.appledash.saneeconomy.utils.database.DatabaseCredentials;
import org.appledash.saneeconomy.utils.database.MySQLConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by appledash on 9/20/16.
 * Blackjack is best pony.
 */
public class TransactionLoggerMySQL implements TransactionLogger {
    private final MySQLConnection dbConn;

    public TransactionLoggerMySQL(DatabaseCredentials credentials) {
        this.dbConn = new MySQLConnection(credentials);
    }

    private void logGeneric(String from, String to, double change, TransactionReason reason) {
        this.dbConn.executeAsyncOperation((conn) -> {
            try {
            PreparedStatement ps = conn.prepareStatement(String.format("INSERT INTO `%s` (`source`, `destination`, `amount`, `reason`) VALUES (?, ?, ?, ?)", dbConn.getTable("transaction_logs")));
                ps.setString(1, from);
                ps.setString(2, to);
                ps.setDouble(3, change);
                ps.setString(4, reason.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Error occurred logging addition", e);
            }
        });
    }

    public boolean testConnection() {
        if (dbConn.testConnection()) {
            createTables();
            return true;
        }

        return false;
    }

    private void createTables() {
        try (Connection conn = dbConn.openConnection()) {
            PreparedStatement ps = conn.prepareStatement(String.format("CREATE TABLE IF NOT EXISTS `%s` (`source` VARCHAR(128), `destination` VARCHAR(128), `amount` DECIMAL(18, 2), `reason` VARCHAR(128), `logged` TIMESTAMP NOT NULL default CURRENT_TIMESTAMP)", dbConn.getTable("transaction_logs")));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create transaction logger tables", e);
        }
    }

    @Override
    public void logTransaction(Transaction transaction) {
        logGeneric(transaction.getSender().getUniqueIdentifier(), transaction.getReceiver().getUniqueIdentifier(), transaction.getAmount(), transaction.getReason());
    }
}
