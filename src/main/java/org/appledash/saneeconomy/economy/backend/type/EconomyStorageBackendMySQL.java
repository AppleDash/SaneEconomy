package org.appledash.saneeconomy.economy.backend.type;

import org.appledash.saneeconomy.economy.backend.EconomyStorageBackend;
import org.bukkit.entity.Player;

import java.sql.*;

/**
 * Created by AppleDash on 6/14/2016.
 * Blackjack is still best pony.
 */
public class EconomyStorageBackendMySQL implements EconomyStorageBackend {
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
            createTable();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void createTable() {
        Connection conn = openConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("CREATE TABLE IF NOT EXISTS `player_balances` (player_uuid CHAR(38), balance DECIMAL(18, 2))");
            ps.executeUpdate();
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create tables!", e);
        }
    }

    @Override
    public boolean accountExists(Player player) {
        return accountExists(player, openConnection());
    }

    @Override
    public double getBalance(Player player) {
        Connection conn = openConnection();
        try {
            PreparedStatement statement = conn.prepareStatement("SELECT balance FROM `player_balances` WHERE `player_uuid` = ?");
            statement.setString(1, player.getUniqueId().toString());

            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                double bal = rs.getDouble("balance");
                conn.close();
                return bal;
            }

            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException("SQL error has occurred.", e);
        }

        return 0.0D;
    }

    @Override
    public void setBalance(Player player, double newBalance) {
        Connection conn = openConnection();
        ensureAccountExists(player, conn);
        try {
            PreparedStatement statement = conn.prepareStatement("UPDATE `player_balances` SET balance = ? WHERE `player_uuid` = ?");
            statement.setDouble(1, newBalance);
            statement.setString(2, player.getUniqueId().toString());
            statement.executeUpdate();
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException("SQL error has occurred.", e);
        }
    }

    @Override
    public double addBalance(Player player, double amount) {
        // TODO: Optimize?
        double curBalance = getBalance(player);
        double newBalance = curBalance + amount;

        setBalance(player, newBalance);

        return newBalance;
    }

    @Override
    public double subtractBalance(Player player, double amount) {
        // TODO: Optimize?
        double curBalance = getBalance(player);
        double newBalance = curBalance - amount;

        setBalance(player, newBalance);

        return newBalance;
    }

    private void ensureAccountExists(Player player, Connection conn) {
        if (!accountExists(player, conn)) {
            try {
                PreparedStatement statement = conn.prepareStatement("INSERT INTO `player_balances` (player_uuid, balance) VALUES (?, 0.0)");
                statement.setString(1, player.getUniqueId().toString());
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("SQL error has occurred.", e);
            }
        }
    }

    private boolean accountExists(Player player, Connection conn) {
        try {
            PreparedStatement statement = conn.prepareStatement("SELECT 1 FROM `player_balances` WHERE `player_uuid` = ?");
            statement.setString(1, player.getUniqueId().toString());

            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                return true;
            }

        } catch (SQLException e) {
            throw new RuntimeException("SQL error has occurred.", e);
        }

        return false;
    }
}
