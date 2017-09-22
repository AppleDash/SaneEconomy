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

        return computeInt(secondParts) > computeInt(firstParts);
    }

    private static int[] intifyParts(String version) {
        String[] firstParts = version.split("\\.");

        return new int[] { Integer.valueOf(firstParts[0]), Integer.valueOf(firstParts[1]), Integer.valueOf(firstParts[2]) };
    }

    private static int computeInt(int[] parts) {
        return (parts[0] * 1000000) + (parts[1] * 1000) + parts[2];
    }
}
