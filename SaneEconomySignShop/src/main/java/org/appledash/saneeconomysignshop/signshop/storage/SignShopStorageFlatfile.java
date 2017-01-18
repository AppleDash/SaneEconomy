package org.appledash.saneeconomysignshop.signshop.storage;

import com.google.common.collect.ImmutableMap;
import org.appledash.saneeconomysignshop.signshop.SignShop;
import org.appledash.saneeconomysignshop.util.SerializableLocation;
import org.bukkit.Location;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by appledash on 10/6/16.
 * Blackjack is best pony.
 */
public class SignShopStorageFlatfile implements SignShopStorage {
    private Map<Location, SignShop> cachedSignShops;
    private File file;

    public SignShopStorageFlatfile(File file) {
        this.file = file;
    }

    @Override
    public void loadSignShops() {
        readSignShops();
    }

    @Override
    public synchronized void putSignShop(SignShop signShop) {
        cachedSignShops.put(signShop.getLocation(), signShop);
        writeSignShops();
    }

    @Override
    public synchronized void removeSignShop(SignShop signShop) {
        cachedSignShops.remove(signShop.getLocation());
        writeSignShops();
    }

    @Override
    public Map<Location, SignShop> getSignShops() {
        return ImmutableMap.copyOf(cachedSignShops);
    }

    private void readSignShops() {
        cachedSignShops = new ConcurrentHashMap<>();

        if (!file.exists()) {
            return;
        }

        try {
            ObjectInput ois = new ObjectInputStream(new FileInputStream(file));
            Map<SerializableLocation, SignShop> tempMap = ((Map<SerializableLocation, SignShop>) ois.readObject());
            tempMap.forEach((sLoc, shop) -> {
                cachedSignShops.put(sLoc.getBukkitLocation(), shop);
            });
            ois.close();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load sign shop date!", e);
        }
    }

    private void writeSignShops() {
        if (file.exists()) {
            file.delete();
        }

        try {
            ObjectOutput oos = new ObjectOutputStream(new FileOutputStream(file));
            Map<SerializableLocation, SignShop> tempMap = new HashMap<>();
            cachedSignShops.forEach((loc, shop) -> {
                tempMap.put(new SerializableLocation(loc), shop);
            });
            oos.writeObject(tempMap);
            oos.close();
        } catch (Exception e) {
            throw new RuntimeException("Failed to save sign shop date!", e);
        }
    }
}
