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
    public static double parsePositiveDouble(String sDouble) {
        try {
            if (Strings.isNullOrEmpty(sDouble)) {
                throw new NumberFormatException();
            }

            sDouble = sDouble.trim();

            if (sDouble.equalsIgnoreCase("nan") || sDouble.equalsIgnoreCase("infinity") || sDouble.equalsIgnoreCase("-infinity")) {
                throw new NumberFormatException();
            }

            double doub;

            try {
                doub = NumberFormat.getInstance().parse(sDouble).doubleValue();
            } catch (ParseException e) {
                throw new NumberFormatException();
            }

            if (doub < 0) {
                throw new NumberFormatException();
            }

            return doub;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static double filterAmount(Currency currency, double amount) {
        try {
            return NumberFormat.getInstance().parse(currency.getFormat().format(amount)).doubleValue();
        } catch (ParseException e) {
            throw new NumberFormatException();
        }
    }

    public static double parseAndFilter(Currency currency, String sDouble) {
        return filterAmount(currency, parsePositiveDouble(sDouble));
    }
}
