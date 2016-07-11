package org.appledash.saneeconomy.utils;

import com.google.common.base.Strings;
import org.appledash.saneeconomy.SaneEconomy;

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

            double doub = Double.valueOf(sDouble);

            if (doub < 0) {
                throw new NumberFormatException();
            }

            return doub;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static double filterAmount(double amount) {
        return Double.valueOf(SaneEconomy.getInstance().getEconomyManager().getCurrency().getFormat().format(amount));
    }

    public static double parseAndFilter(String sDouble) {
        return filterAmount(parsePositiveDouble(sDouble));
    }
}
