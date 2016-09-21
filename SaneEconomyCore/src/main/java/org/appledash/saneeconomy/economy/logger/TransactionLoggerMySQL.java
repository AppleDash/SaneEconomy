package org.appledash.saneeconomy.economy.logger;

import org.appledash.saneeconomy.economy.transaction.Transaction;
import org.appledash.saneeconomy.economy.transaction.TransactionReason;
import org.appledash.saneeconomy.utils.DatabaseCredentials;
import org.appledash.saneeconomy.utils.MySQLConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by appledash on 9/20/16.
 * Blackjack is best pony.
 */
public class TransactionLoggerMySQL implements TransactionLogger {
    private MySQLConnection dbConn;

    public TransactionLoggerMySQL(DatabaseCredentials credentials) {
        this.dbConn = new MySQLConnection(credentials);
    }

    private void logGeneric(String from, String to, double change, TransactionReason reason) {
        this.dbConn.executeAsyncOperation((conn) -> {
            try {
                PreparedStatement ps = conn.prepareStatement("INSERT INTO transaction_logs (`source`, `destination`, `amount`, `reason`) VALUES (?, ?, ?, ?)");
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
            PreparedStatement ps = conn.prepareStatement("CREATE TABLE IF NOT EXISTS `transaction_logs` (`source` VARCHAR(128), `destination` VARCHAR(128), `amount` DECIMAL(18, 2), `reason` VARCHAR(128))");
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
