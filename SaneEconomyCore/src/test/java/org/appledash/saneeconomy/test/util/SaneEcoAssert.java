package org.appledash.saneeconomy.test.util;

import org.appledash.saneeconomy.utils.NumberUtils;
import org.junit.Assert;

import java.math.BigDecimal;

public final class SaneEcoAssert {
    private SaneEcoAssert() {
    }

    public static void assertEquals(BigDecimal left, BigDecimal right) {
        Assert.assertTrue(String.format("%s != %s", left.toPlainString(), right.toPlainString()), NumberUtils.equals(left, right));
    }

    public static void assertEquals(String message, BigDecimal left, BigDecimal right) {
        Assert.assertTrue(message, NumberUtils.equals(left, right));
    }
}
