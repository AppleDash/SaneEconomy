package org.appledash.saneeconomy.utils;

import com.google.common.base.Strings;
import org.appledash.saneeconomy.economy.Currency;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

/**
 * Created by AppleDash on 6/14/2016.
 * Blackjack is still best pony.
 */
public class NumberUtils {
    private static final double INVALID_DOUBLE = -1;

    public static double parsePositiveDouble(String sDouble, DecimalFormat decimalFormat) {
        if (Strings.isNullOrEmpty(sDouble)) {
            return INVALID_DOUBLE;
        }

        sDouble = sDouble.trim();

        if (sDouble.equalsIgnoreCase("nan") || sDouble.equalsIgnoreCase("infinity") || sDouble.equalsIgnoreCase("-infinity")) {
            return INVALID_DOUBLE;
        }

        double doub;

        try {
            doub = decimalFormat.parse(sDouble).doubleValue();
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

    public static double filterAmount(Currency currency, double amount, DecimalFormat decimalFormat) {
        try {
            return NumberFormat.getNumberInstance(Locale.ENGLISH).parse(currency.getFormat().format(amount)).doubleValue();
        } catch (ParseException e) {
            throw new NumberFormatException();
        }
    }

    public static double parseAndFilter(Currency currency, String sDouble, DecimalFormat decimalFormat) {
        return filterAmount(currency, parsePositiveDouble(sDouble, decimalFormat), decimalFormat);
    }
}
