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
    private static final int SCHEMA_VERSION = 2;
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

            if (schemaVer == 1) {
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

            ois.close();
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            SaneEconomy.logger().severe("Failed to load flatfile database!");
            e.printStackTrace();
        }
    }

    @Override
    public void reloadUser(String uniqueId) {
        reloadDatabase(); // not implemented
    }

    private void loadSchemaVersion1(File file) {
        SaneEconomy.logger().info("Upgrading flatfile database from version 1.");
        try {
            Files.copy(file, new File(file.getParentFile(), file.getName() + "-backup"));
            SaneEconomy.logger().info("Backed up old flatfile database.");
        } catch (IOException e) {
            throw new RuntimeException("Failed to back up flatfile database!");
        }

        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
            ois.readInt(); // We already know it's 1.


            Map<UUID, Double> oldBalances = (Map<UUID, Double>) ois.readObject();
            oldBalances.forEach((uuid, balance) -> balances.put("player:" + uuid, balance));

            ois.close();


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
            oos.close();
        } catch (IOException e) {
            SaneEconomy.logger().severe("Failed to save flatfile database!");
        }
    }

    @Override
    public synchronized void setBalance(Economable player, double newBalance) {
        balances.put(player.getUniqueIdentifier(), newBalance);
        saveDatabase();
    }

    @Override
    public void waitUntilFlushed() {
        // Do nothing, database is automatically flushed on every write.
    }
}
