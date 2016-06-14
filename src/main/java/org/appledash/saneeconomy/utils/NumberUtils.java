package org.appledash.saneeconomy.utils;

/**
 * Created by AppleDash on 6/14/2016.
 * Blackjack is still best pony.
 */
public class NumberUtils {
    public static double parsePositiveDouble(String sDouble) {
        try {
            double doub = Double.valueOf(sDouble);

            if (doub < 0) {
                throw new NumberFormatException();
            }

            return doub;
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
