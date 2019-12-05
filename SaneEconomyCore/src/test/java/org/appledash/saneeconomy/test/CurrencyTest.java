package org.appledash.saneeconomy.test;

import org.appledash.saneeconomy.economy.Currency;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * Created by AppleDash on 7/29/2016.
 * Blackjack is still best pony.
 */
public class CurrencyTest {
    @Test
    public void testCurrencyFormat() {
        Currency currency = new Currency("test dollar", "test dollars", new DecimalFormat("0.00"));
        Assert.assertEquals(currency.formatAmount(new BigDecimal(1.0D)), "1.00 test dollar");
        Assert.assertEquals(currency.formatAmount(new BigDecimal(1337.0D)), "1337.00 test dollars");
    }
}
