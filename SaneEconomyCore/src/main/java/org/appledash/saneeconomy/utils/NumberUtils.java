package org.appledash.saneeconomy.utils;

import com.google.common.base.Strings;
import org.appledash.saneeconomy.economy.Currency;

import java.text.NumberFormat;
import java.text.ParseException;

/**
 * Created by AppleDash on 6/14/2016.
 * Blackjack is still best pony.
 */
public class NumberUtils {
    private static final double INVALID_DOUBLE = -1;

    public static double parsePositiveDouble(String sDouble) {
        if (Strings.isNullOrEmpty(sDouble)) {
            return INVALID_DOUBLE;
        }

        sDouble = sDouble.trim();

        if (sDouble.equalsIgnoreCase("nan") || sDouble.equalsIgnoreCase("infinity") || sDouble.equalsIgnoreCase("-infinity")) {
            return INVALID_DOUBLE;
        }

        double doub;

        try {
            doub = NumberFormat.getInstance().parse(sDouble).doubleValue();
        } catch (ParseException | NumberFormatException e) {
            return INVALID_DOUBLE;
        }

        if (doub < 0) {
            return INVALID_DOUBLE;
        }

        if (Double.isInfinite(doub) || Double.isNaN(doub)) {
            return INVALID_DOUBLE;
        }

        return doub;
    }

    public static double filterAmount(Currency currency, double amount) {
        try {
            return NumberFormat.getInstance().parse(currency.getFormat().format(Math.abs(amount))).doubleValue();
        } catch (ParseException e) {
            throw new NumberFormatException();
        }
    }

    public static double parseAndFilter(Currency currency, String sDouble) {
        return filterAmount(currency, parsePositiveDouble(sDouble));
    }
}
