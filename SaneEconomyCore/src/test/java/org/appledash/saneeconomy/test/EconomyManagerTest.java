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
import org.appledash.saneeconomy.test.util.SaneEcoAssert;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
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
        this.economyManager = new EconomyManager(new MockSaneEconomy(),
                                            new Currency("test dollar", "test dollars", new DecimalFormat("0.00")),
                                            new MockEconomyStorageBackend(), null);
    }

    @Test
    public void testEconomyManager() {
        Economable playerOne = Economable.wrap(new MockOfflinePlayer("One"));
        Economable playerTwo = Economable.wrap(new MockOfflinePlayer("Two"));

        // Accounts should not exist
        Assert.assertFalse(this.economyManager.accountExists(playerOne));
        Assert.assertFalse(this.economyManager.accountExists(playerTwo));
        SaneEcoAssert.assertEquals(BigDecimal.ZERO, this.economyManager.getBalance(playerOne));
        SaneEcoAssert.assertEquals(BigDecimal.ZERO, this.economyManager.getBalance(playerTwo));

        this.economyManager.setBalance(playerOne, new BigDecimal("100.0"));

        // Now one should have an account, but two should not
        Assert.assertTrue(this.economyManager.accountExists(playerOne));
        Assert.assertFalse(this.economyManager.accountExists(playerTwo));

        // One should have balance, two should not
        SaneEcoAssert.assertEquals(new BigDecimal("100.00"), this.economyManager.getBalance(playerOne));
        SaneEcoAssert.assertEquals(BigDecimal.ZERO, this.economyManager.getBalance(playerTwo));

        // One should be able to transfer to two
        Assert.assertSame(this.economyManager.transact(new Transaction(this.economyManager.getCurrency(), playerOne, playerTwo, new BigDecimal("50.0"), TransactionReason.PLAYER_PAY)).getStatus(), TransactionResult.Status.SUCCESS);

        // One should now have only 50 left, two should have 50 now
        SaneEcoAssert.assertEquals("Player one should have 50 dollars", new BigDecimal("50.00"), this.economyManager.getBalance(playerOne));
        SaneEcoAssert.assertEquals("Player two should have 50 dollars", new BigDecimal("50.00"), this.economyManager.getBalance(playerTwo));

        // Ensure that balance addition and subtraction works...
        SaneEcoAssert.assertEquals(new BigDecimal("25.00"), this.economyManager.transact(
                                new Transaction(this.economyManager.getCurrency(), playerOne, Economable.CONSOLE, new BigDecimal("25.00"), TransactionReason.TEST_TAKE)
                            ).getFromBalance());

        SaneEcoAssert.assertEquals(new BigDecimal("50.00"), this.economyManager.transact(
                                new Transaction(this.economyManager.getCurrency(), Economable.CONSOLE, playerOne, new BigDecimal("25.00"), TransactionReason.TEST_GIVE)
                            ).getToBalance());

        Assert.assertEquals(TransactionResult.Status.ERR_NOT_ENOUGH_FUNDS, this.economyManager.transact(
                                new Transaction(this.economyManager.getCurrency(), playerTwo, Economable.CONSOLE, new BigDecimal(Double.MAX_VALUE), TransactionReason.TEST_TAKE)
                            ).getStatus());

        // Ensure that hasBalance works
        Assert.assertTrue(this.economyManager.hasBalance(playerOne, new BigDecimal("50.00")));
        Assert.assertFalse(this.economyManager.hasBalance(playerOne, new BigDecimal("51.00")));
    }

    @Test
    public void testTopBalances() {
        Random random = new Random();
        List<Economable> economables = new ArrayList<>(10);
        Set<String> names = new HashSet<>();

        for (int i = 0; i < 10; i++) {
            Economable economable = Economable.wrap(new MockOfflinePlayer("Dude" + i));
            names.add("Dude" + i);
            economables.add(economable);
            this.economyManager.setBalance(economable, new BigDecimal(random.nextInt(1000)));
        }

        this.economyManager.getBackend().reloadTopPlayerBalances();

        List<BigDecimal> javaSortedBalances = economables.stream().map(this.economyManager::getBalance).sorted((left, right) -> -left.compareTo(right)).collect(Collectors.toList());
        List<BigDecimal> ecoManTopBalances = ImmutableList.copyOf(this.economyManager.getTopBalances(10, 0).values());

        Assert.assertTrue("List is not correctly sorted!", this.areListsEqual(javaSortedBalances, ecoManTopBalances));
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
