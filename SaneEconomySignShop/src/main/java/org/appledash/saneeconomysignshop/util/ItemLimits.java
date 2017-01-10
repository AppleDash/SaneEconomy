package org.appledash.saneeconomysignshop.util;

/**
 * Created by appledash on 1/1/17.
 * Blackjack is still best pony.
 */
public class ItemLimits {
    // The default limit for items that have no limit.
    public static final ItemLimits DEFAULT = new ItemLimits(10, 1);

    private final int limit;
    private final int hourlyGain;

    public ItemLimits(int limit, int hourlyGain) {
        this.limit = limit;
        this.hourlyGain = hourlyGain;
    }

    public int getHourlyGain() {
        return hourlyGain;
    }

    public int getLimit() {
        return limit;
    }
}
