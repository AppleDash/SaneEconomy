package org.appledash.saneeconomy.test;

import org.appledash.saneeconomy.economy.Currency;
import org.junit.Assert;
import org.junit.Test;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by AppleDash on 7/29/2016.
 * Blackjack is still best pony.
 */
public class CurrencyTest {

    private final static DecimalFormat FORMAT = (DecimalFormat) NumberFormat.getNumberInstance(Locale.US);
    static {
        FORMAT.applyPattern("0.00");
    }

    @Test
    public void testCurrencyFormat() {
        Currency currency = new Currency("test dollar", "test dollars", FORMAT);
        Assert.assertEquals("1.00 test dollar", currency.formatAmount(1.0D));
        Assert.assertEquals("1337.00 test dollars", currency.formatAmount(1337.0D));
    }
}
