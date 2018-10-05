package org.appledash.saneeconomy.economy.backend.type;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.appledash.saneeconomy.economy.economable.Economable;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by appledash on 1/22/17.
 * Blackjack is best pony.
 */
public class EconomyStorageBackendJSON extends EconomyStorageBackendCaching {
    private final Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
    private File file;

    public EconomyStorageBackendJSON(File file) {
        this.file = file;
    }

    @Override
    public void setBalance(Economable economable, double newBalance) {
        balances.put(economable.getUniqueIdentifier(), newBalance);
        saveDatabase();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void reloadDatabase() {
        if (!file.exists()) {
            return;
        }

        try {
            balances = new ConcurrentHashMap<>((Map)gson.fromJson(new FileReader(file), new TypeToken<Map<String, Double>>(){}.getType()));
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Failed to load database!", e);
        }
    }

    @Override
    public void waitUntilFlushed() {
        // NOOP - Database is saved on every change.
    }

    private synchronized void saveDatabase() {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file, false))) {
            bufferedWriter.write(gson.toJson(balances));
        } catch (IOException e) {
            throw new RuntimeException("Failed to save database", e);
        }
    }
}
