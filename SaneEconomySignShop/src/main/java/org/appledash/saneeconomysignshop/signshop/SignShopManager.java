package org.appledash.saneeconomysignshop.signshop;

import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by appledash on 10/2/16.
 * Blackjack is still best pony.
 */
public class SignShopManager {
    private Map<Location, SignShop> signShops = new HashMap<Location, SignShop>();

    public void loadSignShops() {

    }

    public void addSignShop(SignShop signShop) {
        signShops.put(signShop.getLocation(), signShop);
    }

    public Optional<SignShop> getSignShop(Location location) {
        return Optional.ofNullable(signShops.get(location));
    }
}
