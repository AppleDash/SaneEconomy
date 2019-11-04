package org.appledash.saneeconomy.economy.backend.type;

import com.avaje.ebeaninternal.server.cluster.DataHolder;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import org.appledash.saneeconomy.economy.economable.Economable;

import java.io.*;
import java.math.BigDecimal;
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
    public void setBalance(Economable economable, BigDecimal newBalance) {
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
            // try to load the old format and convert it
            // if that fails, load the new format
            DataHolderOld dataHolder = gson.fromJson(new FileReader(file), DataHolderOld.class);
            this.balances = new ConcurrentHashMap<>();
            this.uuidToName = new ConcurrentHashMap<>(dataHolder.uuidToName);

            dataHolder.balances.forEach((s, bal) -> {
                this.balances.put(s, new BigDecimal(bal));
            });

            this.saveDatabase();
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Failed to load database!", e);
        } catch (Exception e) {
            try {
                DataHolder dataHolder = gson.fromJson(new FileReader(file), DataHolder.class);
                this.balances = new ConcurrentHashMap<>(dataHolder.balances);
                this.uuidToName = new ConcurrentHashMap<>(dataHolder.uuidToName);
            } catch (FileNotFoundException ex) {
                throw new RuntimeException("Failed to load database!", e);
            }
        }
    }

    @Override
    public void waitUntilFlushed() {
        // NOOP - Database is saved on every change.
    }

    private synchronized void saveDatabase() {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file, false))) {
            DataHolder dataHolder = new DataHolder(this.balances, this.uuidToName);
            bufferedWriter.write(gson.toJson(dataHolder));
        } catch (IOException e) {
            throw new RuntimeException("Failed to save database", e);
        }
    }

    private static class DataHolderOld {
        @SerializedName("balances")
        private Map<String, Double> balances;
        @SerializedName("uuidToName")
        private Map<String, String> uuidToName;

        public DataHolderOld(Map<String, Double> balances, Map<String, String> uuidToName) {
            this.balances = balances;
            this.uuidToName = uuidToName;
        }
    }

    private static class DataHolder {
        @SerializedName("balances")
        private Map<String, BigDecimal> balances;
        @SerializedName("uuidToName")
        private Map<String, String> uuidToName;

        public DataHolder(Map<String, BigDecimal> balances, Map<String, String> uuidToName) {
            this.balances = balances;
            this.uuidToName = uuidToName;
        }
    }
}
