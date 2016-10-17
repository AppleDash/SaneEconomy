package org.appledash.saneeconomysignshop.signshop.storage;

import org.appledash.saneeconomysignshop.signshop.SignShop;
import org.bukkit.Location;

import java.util.Map;

/**
 * Created by appledash on 10/6/16.
 * Blackjack is best pony.
 */
public interface SignShopStorage {
    void loadSignShops();
    void putSignShop(SignShop signShop);
    void removeSignShop(SignShop signShop);
    Map<Location, SignShop> getSignShops();
}
