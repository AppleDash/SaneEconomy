package org.appledash.saneeconomy.economy.backend.type;

import com.google.common.io.Files;
import org.appledash.saneeconomy.SaneEconomy;
import org.appledash.saneeconomy.economy.economable.Economable;

import java.io.*;
import java.util.Map;
import java.util.UUID;

/**
 * Created by AppleDash on 6/13/2016.
 * Blackjack is still best pony.
 */
public class EconomyStorageBackendFlatfile extends EconomyStorageBackendCaching {
    private static final int SCHEMA_VERSION = 3;
    private final File file;

    public EconomyStorageBackendFlatfile(File file) {
        this.file = file;
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized void reloadDatabase() {
        if (!file.exists()) {
            return;
        }

        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
            int schemaVer = ois.readInt();

            if (schemaVer == 2) {
                ois.close();
                loadSchemaVersion1(file);
                return;
            }

            if (schemaVer != SCHEMA_VERSION) {
                // ???
                SaneEconomy.logger().severe("Unrecognized flatfile database version " + schemaVer + ", cannot load database!");
                return;
            }

            balances = (Map<String, Double>) ois.readObject();
            uuidToName = (Map<String, String>) ois.readObject();

            ois.close();
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            SaneEconomy.logger().severe("Failed to load flatfile database!");
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void loadSchemaVersion1(File file) {
        SaneEconomy.logger().info("Upgrading flatfile database from version 2.");
        try {
            Files.copy(file, new File(file.getParentFile(), file.getName() + "-backup"));
            SaneEconomy.logger().info("Backed up old flatfile database.");
        } catch (IOException e) {
            throw new RuntimeException("Failed to back up flatfile database!");
        }

        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
            ois.readInt(); // We already know it's 2.
            this.balances = (Map<String, Double>) ois.readObject();

            /* Yes, this is kind of bad, but we want to make sure we're loading AND saving the new version of the DB. */
            saveDatabase();
            reloadDatabase();
        } catch (IOException | ClassNotFoundException e) {
            SaneEconomy.logger().severe("Failed to upgrade flatfile database! Recommend reporting this bug and reverting to an older version of the plugin.");
            throw new RuntimeException("Failed to upgrade flatfile database!", e);
        }
    }

    private void saveDatabase() {
        if (file.exists()) {
            file.delete();
        }

        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
            oos.writeInt(SCHEMA_VERSION);
            oos.writeObject(balances);
            oos.writeObject(uuidToName);
            oos.close();
        } catch (IOException e) {
            SaneEconomy.logger().severe("Failed to save flatfile database!");
        }
    }

    @Override
    public synchronized void setBalance(Economable economable, double newBalance) {
        this.balances.put(economable.getUniqueIdentifier(), newBalance);
        this.uuidToName.put(economable.getUniqueIdentifier(), economable.getName());
        saveDatabase();
    }

    @Override
    public void waitUntilFlushed() {
        // Do nothing, database is automatically flushed on every write.
    }
}
