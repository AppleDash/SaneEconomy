package org.appledash.saneeconomy.updates;

/**
 * Created by appledash on 7/15/17.
 * Blackjack is best pony.
 */
public class VersionComparer {
    public static boolean isSemVerGreaterThan(String first, String second) {
        if (first == null) {
            return true;
        }

        if (second == null) {
            return false;
        }

        int[] firstParts = intifyParts(first);
        int[] secondParts = intifyParts(second);

        if (secondParts[0] > firstParts[0]) {
            return true;
        }

        if (secondParts[1] > firstParts[1]) {
            return true;
        }

        return secondParts[2] > firstParts[2];
    }

    private static int[] intifyParts(String version) {
        String[] firstParts = version.split("\\.");

        return new int[] { Integer.valueOf(firstParts[0]), Integer.valueOf(firstParts[1]), Integer.valueOf(firstParts[2]) };
    }
}
