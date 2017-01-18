package org.appledash.saneeconomysignshop.signshop.storage;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.appledash.saneeconomysignshop.signshop.SignShop;
import org.bukkit.Location;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by appledash on 1/18/17.
 * Blackjack is still best pony.
 */
public class SignShopStorageJSON implements SignShopStorage {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
    private final File storageFile;
    private Map<Location, SignShop> cachedSignShops = new HashMap<>();

    public SignShopStorageJSON(File storageFile) {
        this.storageFile = storageFile;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void loadSignShops() {
        if (!storageFile.exists()) {
            return;
        }

        try {
            List<SignShop> tempShops = gson.fromJson(new FileReader(storageFile), new TypeToken<List<SignShop>>(){}.getType());
            tempShops.forEach((shop) -> cachedSignShops.put(shop.getLocation(), shop));
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("This shouldn't happen - the file " + storageFile.getAbsolutePath() + " disappeared while we were trying to read it!", e);
        }

        saveSignShops();
    }

    @Override
    public void putSignShop(SignShop signShop) {
        cachedSignShops.put(signShop.getLocation(), signShop);
        saveSignShops();
    }

    @Override
    public void removeSignShop(SignShop signShop) {
        cachedSignShops.remove(signShop.getLocation());
        saveSignShops();
    }

    private synchronized void saveSignShops() {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(storageFile, false))) {
            bufferedWriter.write(gson.toJson(ImmutableList.copyOf(cachedSignShops.values())));
            bufferedWriter.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to save sign shops!", e);
        }
    }

    @Override
    public Map<Location, SignShop> getSignShops() {
        return ImmutableMap.copyOf(cachedSignShops);
    }
}
