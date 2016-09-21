package org.appledash.saneeconomy.economy.logger;

import org.appledash.saneeconomy.economy.TransactionReason;
import org.appledash.saneeconomy.economy.economable.Economable;
import org.appledash.saneeconomy.utils.DatabaseCredentials;
import org.appledash.saneeconomy.utils.MySQLConnection;

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

    @Override
    public void logAddition(Economable economable, double amount, TransactionReason reason) {
        logGeneric(reason.toString(), economable.getUniqueIdentifier(), amount);
    }

    @Override
    public void logSubtraction(Economable economable, double amount, TransactionReason reason) {
        logGeneric(reason.toString(), economable.getUniqueIdentifier(), -amount);
    }

    @Override
    public void logTransfer(Economable from, Economable to, double amount) {
        logGeneric(from.getUniqueIdentifier(), to.getUniqueIdentifier(), amount);
    }

    private void logGeneric(String from, String to, double change) {
        this.dbConn.executeAsyncOperation((conn) -> {
            try {
                PreparedStatement ps = conn.prepareStatement("INSERT INTO transaction_logs (`source`, `destination`, `amount`) VALUES (?, ?, ?)");
                ps.setString(1, from);
                ps.setString(2, to);
                ps.setDouble(3, change);
            } catch (SQLException e) {
                throw new RuntimeException("Error occurred logging addition", e);
            }
        });
    }
}
