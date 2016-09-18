package org.appledash.saneeconomy.utils;

/**
 * Created by appledash on 9/18/16.
 * Blackjack is best pony.
 */
public class DatabaseCredentials {
    private final String hostname;
    private final int port;
    private final String username;
    private final String password;
    private final String databaseName;

    public DatabaseCredentials(String hostname, int port, String username, String password, String databaseName) {
        this.hostname = hostname;
        this.port = port;
        this.username = username;
        this.password = password;
        this.databaseName = databaseName;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getJDBCURL() {
        return String.format("jdbc:mysql://%s:%d/%s", hostname, port, databaseName);
    }
}
