package org.appledash.saneeconomy.economy.backend.type;

import org.appledash.saneeconomy.SaneEconomy;
import org.appledash.saneeconomy.economy.backend.EconomyStorageBackend;
import org.appledash.saneeconomy.utils.MapUtil;
import org.bukkit.OfflinePlayer;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by AppleDash on 6/13/2016.
 * Blackjack is still best pony.
 */
public class EconomyStorageBackendFlatfile implements EconomyStorageBackend {
    private static final int SCHEMA_VERSION = 1;

    private final File file;
    private Map<UUID, Double> playerBalances = new HashMap<>();
    private Map<UUID, Double> topBalances = new LinkedHashMap<>();

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

    @Override
    public void reloadTopBalances() {
        topBalances = MapUtil.sortByValue(playerBalances);
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
    public synchronized boolean accountExists(OfflinePlayer player) {
        return playerBalances.containsKey(player.getUniqueId());
    }

    @Override
    public synchronized double getBalance(OfflinePlayer player) {
        if (!playerBalances.containsKey(player.getUniqueId())) {
            return 0.0D;
        }

        return playerBalances.get(player.getUniqueId());
    }

    @Override
    public synchronized void setBalance(OfflinePlayer player, double newBalance) {
        playerBalances.put(player.getUniqueId(), newBalance);
        saveDatabase();
    }

    @Override
    public synchronized double addBalance(OfflinePlayer player, double amount) {
        double newAmount = getBalance(player) + amount;

        setBalance(player, newAmount);

        return newAmount;
    }

    @Override
    public synchronized double subtractBalance(OfflinePlayer player, double amount) {
        double newAmount = getBalance(player) - amount;

        setBalance(player, newAmount);

        return newAmount;
    }

    @Override
    public Map<UUID, Double> getTopBalances(int amount) {
        return MapUtil.takeFromMap(topBalances, amount);
    }
}
