package org.appledash.saneeconomy.utils.database;

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
    private final String tablePrefix;
    private final int maxRetries;
    private final int queryTimeout;
    private final boolean useSsl;

    public DatabaseCredentials(String hostname, int port, String username, String password, String databaseName, String tablePrefix, boolean useSsl) {
        this.hostname = hostname;
        this.port = port;
        this.username = username;
        this.password = password;
        this.databaseName = databaseName;
        this.tablePrefix = tablePrefix;
        this.useSsl = useSsl;
        maxRetries = 5;
        queryTimeout = 5000;
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
        return String.format("jdbc:mysql://%s:%d/%s?useSSL=%s", hostname, port, databaseName, useSsl);
    }

    public String getTablePrefix() {
        return tablePrefix;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public int getQueryTimeout() {
        return queryTimeout;
    }
}
