package org.appledash.saneeconomy.test;

import org.appledash.saneeconomy.economy.economable.Economable;
import org.appledash.saneeconomy.economy.economable.EconomableFaction;
import org.appledash.saneeconomy.economy.economable.EconomableGeneric;
import org.appledash.saneeconomy.economy.economable.EconomablePlayer;
import org.appledash.saneeconomy.test.mock.MockOfflinePlayer;
import org.bukkit.OfflinePlayer;
import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

/**
 * Created by AppleDash on 7/29/2016.
 * Blackjack is still best pony.
 */
public class EconomableTest {
    @Test
    public void testWrapFaction() {
        UUID uuid = UUID.randomUUID();
        Economable economable = Economable.wrap(String.format("faction-%s", uuid));
        Assert.assertEquals(economable.getClass(), EconomableFaction.class);
        Assert.assertEquals(economable.getUniqueIdentifier(), String.format("faction:%s", uuid));
    }

    @Test
    public void testWrapPlayer() {
        OfflinePlayer dummy = new MockOfflinePlayer("Dummy");
        Economable economable = Economable.wrap(dummy);
        Assert.assertEquals(economable.getClass(), EconomablePlayer.class);
        Assert.assertEquals(economable.getUniqueIdentifier(), String.format("player:%s", dummy.getUniqueId()));
    }

    @Test
    public void testWrapGeneric() {
        Economable economable = Economable.wrap("something");
        Assert.assertEquals(economable.getClass(), EconomableGeneric.class);
        Assert.assertEquals(economable.getUniqueIdentifier(), "generic:something");
    }
}
