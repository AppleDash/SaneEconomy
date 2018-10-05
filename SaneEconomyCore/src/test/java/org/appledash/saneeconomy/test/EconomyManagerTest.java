package org.appledash.saneeconomy.test;

import com.google.common.collect.ImmutableList;
import org.appledash.saneeconomy.economy.Currency;
import org.appledash.saneeconomy.economy.EconomyManager;
import org.appledash.saneeconomy.economy.economable.Economable;
import org.appledash.saneeconomy.economy.transaction.Transaction;
import org.appledash.saneeconomy.economy.transaction.TransactionReason;
import org.appledash.saneeconomy.economy.transaction.TransactionResult;
import org.appledash.saneeconomy.test.mock.MockEconomyStorageBackend;
import org.appledash.saneeconomy.test.mock.MockOfflinePlayer;
import org.appledash.saneeconomy.test.mock.MockSaneEconomy;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by AppleDash on 7/29/2016.
 * Blackjack is still best pony.
 */
public class EconomyManagerTest {
    private EconomyManager economyManager;

    @Before
    public void setupEconomyManager()  {
        economyManager = new EconomyManager(new MockSaneEconomy(),
                new Currency("test dollar", "test dollars", new DecimalFormat("0.00")),
                new MockEconomyStorageBackend(), null);
    }

    @Test
    public void testEconomyManager() {
        Economable playerOne = Economable.wrap(new MockOfflinePlayer("One"));
        Economable playerTwo = Economable.wrap(new MockOfflinePlayer("Two"));

        // Accounts should not exist
        Assert.assertFalse(economyManager.accountExists(playerOne));
        Assert.assertFalse(economyManager.accountExists(playerTwo));
        Assert.assertEquals(0.0D, economyManager.getBalance(playerOne), 0.0);
        Assert.assertEquals(0.0D, economyManager.getBalance(playerTwo), 0.0);

        economyManager.setBalance(playerOne, 100.0D);

        // Now one should have an account, but two should not
        Assert.assertTrue(economyManager.accountExists(playerOne));
        Assert.assertFalse(economyManager.accountExists(playerTwo));

        // One should have balance, two should not
        Assert.assertEquals(100.0, economyManager.getBalance(playerOne), 0.0);
        Assert.assertEquals(0.0, economyManager.getBalance(playerTwo), 0.0);

        // One should be able to transfer to two
        Assert.assertTrue(economyManager.transact(new Transaction(economyManager.getCurrency(), playerOne, playerTwo, 50.0, TransactionReason.PLAYER_PAY)).getStatus() == TransactionResult.Status.SUCCESS);

        // One should now have only 50 left, two should have 50 now
        Assert.assertEquals("Player one should have 50 dollars", 50.0, economyManager.getBalance(playerOne), 0.0);
        Assert.assertEquals("Player two should have 50 dollars", 50.0, economyManager.getBalance(playerTwo), 0.0);

        // Ensure that balance addition and subtraction works...
        Assert.assertEquals(25.0, economyManager.transact(
                new Transaction(economyManager.getCurrency(), playerOne, Economable.CONSOLE, 25.0, TransactionReason.TEST_TAKE)
        ).getFromBalance(), 0.0);

        Assert.assertEquals(50.0, economyManager.transact(
                new Transaction(economyManager.getCurrency(), Economable.CONSOLE, playerOne, 25.0, TransactionReason.TEST_GIVE)
        ).getToBalance(), 0.0);

        Assert.assertEquals(TransactionResult.Status.ERR_NOT_ENOUGH_FUNDS, economyManager.transact(
                new Transaction(economyManager.getCurrency(), playerTwo, Economable.CONSOLE, Double.MAX_VALUE, TransactionReason.TEST_TAKE)
        ).getStatus());

        // Ensure that hasBalance works
        Assert.assertTrue(economyManager.hasBalance(playerOne, 50.0));
        Assert.assertFalse(economyManager.hasBalance(playerOne, 51.0));


    }

    @Test
    public void testTopBalances() {
        Random random = new Random();
        List<Economable> economables = new ArrayList<>(10);
        Set<String> names = new HashSet<String>();

        for (int i = 0; i < 10; i++) {
            Economable economable = Economable.wrap(new MockOfflinePlayer("Dude" + i));
            names.add("Dude" + i);
            economables.add(economable);
            this.economyManager.setBalance(economable, random.nextInt(1000));
        }

        this.economyManager.getBackend().reloadTopPlayerBalances();

        List<Double> javaSortedBalances = economables.stream().map(this.economyManager::getBalance).sorted((left, right) -> -left.compareTo(right)).collect(Collectors.toList());
        List<Double> ecoManTopBalances = ImmutableList.copyOf(this.economyManager.getTopBalances(10, 0).values());

        Assert.assertTrue("List is not correctly sorted!", areListsEqual(javaSortedBalances, ecoManTopBalances));
        Assert.assertEquals("Wrong number of top balances!", 5, this.economyManager.getTopBalances(5, 0).size());

        this.economyManager.getTopBalances(10, 0).keySet().forEach(name -> Assert.assertTrue("Returned name in top balances not valid!", names.contains(name)));
    }

    private <T> boolean areListsEqual(List<T> first, List<T> second) {
        if (first.size() != second.size()) {
            throw new IllegalArgumentException("Lists must be same length (first=" + first.size() + ", second=" + second.size() + ")");
        }

        for (int i = 0; i < first.size(); i++) {
            if (!first.get(i).equals(second.get(i))) {
                return false;
            }
        }

        return true;
    }
}
