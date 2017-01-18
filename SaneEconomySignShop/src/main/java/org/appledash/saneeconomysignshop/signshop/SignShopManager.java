package org.appledash.saneeconomysignshop.signshop;

import org.appledash.saneeconomysignshop.signshop.storage.SignShopStorage;
import org.bukkit.Location;

import java.util.Optional;

/**
 * Created by appledash on 10/2/16.
 * Blackjack is still best pony.
 */
public class SignShopManager {
    private final SignShopStorage storage;

    public SignShopManager(SignShopStorage storage) {
        this.storage = storage;
    }

    public void loadSignShops() {
        storage.loadSignShops();
    }

    public void addSignShop(SignShop signShop) {
        storage.putSignShop(signShop);
    }

    public void removeSignShop(SignShop signShop) {
        storage.removeSignShop(signShop);
    }

    public Optional<SignShop> getSignShop(Location location) {
        return Optional.ofNullable(storage.getSignShops().get(location));
    }
}
