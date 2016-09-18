package org.appledash.saneeconomy.test;

import org.appledash.saneeconomy.economy.Currency;
import org.appledash.saneeconomy.economy.EconomyManager;
import org.appledash.saneeconomy.economy.TransactionReason;
import org.appledash.saneeconomy.economy.economable.Economable;
import org.appledash.saneeconomy.test.mock.MockEconomyStorageBackend;
import org.appledash.saneeconomy.test.mock.MockOfflinePlayer;
import org.junit.Assert;
import org.junit.Test;

import java.text.DecimalFormat;

/**
 * Created by AppleDash on 7/29/2016.
 * Blackjack is still best pony.
 */
public class EconomyManagerTest {
    @Test
    public void testEconomyManager() {
        EconomyManager economyManager = new EconomyManager(new Currency("test dollar", "test dollars", new DecimalFormat("0.00")), new MockEconomyStorageBackend());
        Economable playerOne = Economable.wrap(new MockOfflinePlayer("One"));
        Economable playerTwo = Economable.wrap(new MockOfflinePlayer("Two"));

        // Accounts should not exist
        Assert.assertFalse(economyManager.accountExists(playerOne));
        Assert.assertFalse(economyManager.accountExists(playerTwo));
        Assert.assertEquals(economyManager.getBalance(playerOne), 0.0D, 0.0);
        Assert.assertEquals(economyManager.getBalance(playerTwo), 0.0D, 0.0);

        economyManager.setBalance(playerOne, 100.0D, TransactionReason.PLUGIN);

        // Now one should have an account, but two should not
        Assert.assertTrue(economyManager.accountExists(playerOne));
        Assert.assertFalse(economyManager.accountExists(playerTwo));

        // One should have balance, two should not
        Assert.assertEquals(economyManager.getBalance(playerOne), 100.0, 0.0);
        Assert.assertEquals(economyManager.getBalance(playerTwo), 0.0, 0.0);

        // One should be able to transfer to two
        Assert.assertTrue(economyManager.transfer(playerOne, playerTwo, 50.0));

        // One should now have only 50 left, two should have 50 now
        Assert.assertEquals(economyManager.getBalance(playerOne), 50.0, 0.0);
        Assert.assertEquals(economyManager.getBalance(playerTwo), 50.0, 0.0);

        // Ensure that balance addition and subtraction works...
        Assert.assertEquals(economyManager.subtractBalance(playerOne, 25.0, TransactionReason.PLUGIN), 25.0, 0.0);
        Assert.assertEquals(economyManager.addBalance(playerOne, 25.0, TransactionReason.PLUGIN), 50.0, 0.0);
        Assert.assertEquals(economyManager.subtractBalance(playerTwo, Double.MAX_VALUE, TransactionReason.PLUGIN), 0.0, 0.0);

        // Ensure that hasBalance works
        Assert.assertTrue(economyManager.hasBalance(playerOne, 50.0));
        Assert.assertFalse(economyManager.hasBalance(playerOne, 51.0));


    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeBalance() {
        EconomyManager economyManager = new EconomyManager(new Currency("test dollar", "test dollars", new DecimalFormat("0.00")), new MockEconomyStorageBackend());
        Economable economable = Economable.wrap(new MockOfflinePlayer("Bob"));
        economyManager.setBalance(economable, -1.0, TransactionReason.PLUGIN);
    }
}
