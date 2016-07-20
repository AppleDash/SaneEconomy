package org.appledash.saneeconomy.economy.backend.type;

import org.appledash.saneeconomy.SaneEconomy;
import org.bukkit.OfflinePlayer;

import java.io.*;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by AppleDash on 6/13/2016.
 * Blackjack is still best pony.
 */
public class EconomyStorageBackendFlatfile extends EconomyStorageBackendCaching {
    private static final int SCHEMA_VERSION = 1;
    private final File file;

    public EconomyStorageBackendFlatfile(File file) {
        this.file = file;
    }

    @Override
    public void reloadDatabase() {
        if (!file.exists()) {
            return;
        }

        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
            int schemaVer = ois.readInt();
            if (schemaVer != SCHEMA_VERSION) { // Eventually, if I change the schema there will be code to detect such changes and update it on load.
                // ???
                SaneEconomy.logger().severe("Unrecognized flatfile database version " + schemaVer + ", cannot load database!");
                return;
            }

            playerBalances = (HashMap<UUID, Double>)ois.readObject();

            ois.close();
        } catch (IOException | ClassNotFoundException e) {
            SaneEconomy.logger().severe("Failed to load flatfile database!");
            e.printStackTrace();
        }
    }

    private void saveDatabase() {
        if (file.exists()) {
            file.delete();
        }

        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
            oos.writeInt(SCHEMA_VERSION);
            oos.writeObject(playerBalances);
            oos.close();
        } catch (IOException e) {
            SaneEconomy.logger().severe("Failed to save flatfile database!");
        }
    }

    @Override
    public synchronized void setBalance(OfflinePlayer player, double newBalance) {
        playerBalances.put(player.getUniqueId(), newBalance);
        saveDatabase();
    }
}
