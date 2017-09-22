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
        Assert.assertEquals(69.0, NumberUtils.parsePositiveDouble("69.0"), 0.0);
        // Valid but not positive
        Assert.assertEquals(-1.0, NumberUtils.parsePositiveDouble("-10.0"), 0.0);
        // Invalid
        Assert.assertEquals(-1.0, NumberUtils.parsePositiveDouble("nan"), 0.0);
        Assert.assertEquals(-1.0, NumberUtils.parsePositiveDouble("ponies"), 0.0);
        // Infinite
        Assert.assertEquals(-1.0, NumberUtils.parsePositiveDouble("1E1000000000"), 0.0);
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
        } finally {
            Locale.setDefault(old);
        }
    }
}
