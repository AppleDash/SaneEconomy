package org.appledash.saneeconomy.test;

import org.appledash.saneeconomy.economy.Currency;
import org.appledash.saneeconomy.test.util.SaneEcoAssert;
import org.appledash.saneeconomy.utils.NumberUtils;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
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
        SaneEcoAssert.assertEquals(new BigDecimal("69.0"), NumberUtils.parsePositiveDouble("69.0"));
        // Valid but not positive
        SaneEcoAssert.assertEquals(BigDecimal.ONE.negate(), NumberUtils.parsePositiveDouble("-10.0"));
        // Invalid
        SaneEcoAssert.assertEquals(BigDecimal.ONE.negate(), NumberUtils.parsePositiveDouble("nan"));
        SaneEcoAssert.assertEquals(BigDecimal.ONE.negate(), NumberUtils.parsePositiveDouble("ponies"));
        // Infinite
        // TODO: Not needed with BigDecimal? Assert.assertEquals(BigDecimal.ONE.negate(), NumberUtils.parsePositiveDouble("1E1000000000"));
    }

    @Test
    public void testFilter() {
        Currency currency = new Currency(null, null, new DecimalFormat("0.00"));

        SaneEcoAssert.assertEquals(new BigDecimal("1337.42"), NumberUtils.filterAmount(currency, new BigDecimal("1337.420")));
    }

    @Test
    public void testFilterFrench() {
        Locale old = Locale.getDefault();
        Locale.setDefault(Locale.FRANCE);
        try {
            this.testFilter();
        } catch (Throwable e) {
            Locale.setDefault(old);
            throw e;
        } finally {
            Locale.setDefault(old);
        }
    }

    @Test
    public void testBigDecimalEquals() {
        BigDecimal one = new BigDecimal("100.0");
        BigDecimal two = new BigDecimal("100.00");
        BigDecimal three = new BigDecimal("100.1");

        Assert.assertTrue("100.0 should equal 100.00", NumberUtils.equals(one, two));
        Assert.assertFalse("100.0 should not equal 100.1", NumberUtils.equals(one, three));
    }
}
