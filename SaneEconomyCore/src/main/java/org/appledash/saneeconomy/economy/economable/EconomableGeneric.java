package org.appledash.saneeconomy.economy.economable;

/**
 * Created by appledash on 7/19/16.
 * Blackjack is still best pony.
 */
public class EconomableGeneric implements Economable {
    private String uniqueIdentifier;

    public EconomableGeneric(String uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
    }

    @Override
    public String getUniqueIdentifier() {
        return uniqueIdentifier;
    }
}
