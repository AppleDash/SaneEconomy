package org.appledash.saneeconomy.test;

import org.appledash.saneeconomy.economy.Currency;
import org.appledash.saneeconomy.utils.NumberUtils;
import org.junit.Assert;
import org.junit.Test;

import java.text.DecimalFormat;
import java.util.Locale;

/**
 * Created by AppleDash on 7/29/2016.
 * Blackjack is still best pony.
 */
public class NumberUtilsTest {
    @Test
    public void testParsePositive() {
        // Valid input
        Assert.assertEquals(NumberUtils.parsePositiveDouble("69.0"), 69.0, 0.0);
        // Valid but not positive
        Assert.assertEquals(NumberUtils.parsePositiveDouble("-10.0"), -1.0, 0.0);
        // Invalid
        Assert.assertEquals(NumberUtils.parsePositiveDouble("nan"), -1.0, 0.0);
        Assert.assertEquals(NumberUtils.parsePositiveDouble("ponies"), -1.0, 0.0);
    }

    @Test
    public void testFilter() {
        Currency currency = new Currency(null, null, new DecimalFormat("0.00"));

        Assert.assertEquals(NumberUtils.filterAmount(currency, 1337.420D), 1337.42, 0.0);
    }

    @Test
    public void testFilterFrench() {
        Locale old = Locale.getDefault();
        Locale.setDefault(Locale.FRANCE);
        try {
            testFilter();
        } catch (Throwable e) {
            Locale.setDefault(old);
            throw e;
        }
    }
}
