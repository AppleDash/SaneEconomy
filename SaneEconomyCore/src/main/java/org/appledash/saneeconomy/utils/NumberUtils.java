package org.appledash.saneeconomy.utils;

import com.google.common.base.Strings;
import org.appledash.saneeconomy.economy.Currency;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

/**
 * Created by AppleDash on 6/14/2016.
 * Blackjack is still best pony.
 */
public final class NumberUtils {
    private static final BigDecimal INVALID_DOUBLE = BigDecimal.ONE.negate();

    private NumberUtils() {
    }

    public static BigDecimal parsePositiveDouble(String sDouble) {
        if (Strings.isNullOrEmpty(sDouble)) {
            return INVALID_DOUBLE;
        }

        sDouble = sDouble.trim();

        if (sDouble.equalsIgnoreCase("nan") || sDouble.equalsIgnoreCase("infinity") || sDouble.equalsIgnoreCase("-infinity")) {
            return INVALID_DOUBLE;
        }

        BigDecimal doub;

        try {
            doub = (BigDecimal) constructDecimalFormat().parseObject(sDouble);
        } catch (ParseException | NumberFormatException e) {
            return INVALID_DOUBLE;
        }

        if (doub.compareTo(BigDecimal.ZERO) < 0) {
            return INVALID_DOUBLE;
        }

        /*if (Double.isInfinite(doub) || Double.isNaN(doub)) {
            return INVALID_DOUBLE;
        }*/

        return doub;
    }

    public static BigDecimal filterAmount(Currency currency, BigDecimal amount) {
        try {
            return (BigDecimal) constructDecimalFormat().parse(currency.getFormat().format(amount.abs()));
        } catch (ParseException e) {
            throw new NumberFormatException();
        }
    }

    public static BigDecimal parseAndFilter(Currency currency, String sDouble) {
        return filterAmount(currency, parsePositiveDouble(sDouble));
    }

    public static boolean equals(BigDecimal left, BigDecimal right) {
        if (left == null) {
            throw new NullPointerException("left == null");
        }

        if (right == null) {
            throw new NullPointerException("right == null");
        }

        return left.compareTo(right) == 0;
    }

    private static DecimalFormat constructDecimalFormat() {
        DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getInstance();

        decimalFormat.setParseBigDecimal(true);

        return decimalFormat;
    }
}
