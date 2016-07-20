package org.appledash.saneeconomy.economy.economable;

/**
 * Created by appledash on 7/19/16.
 * Blackjack is still best pony.
 */
public class EconomableFaction implements Economable {
    private String factionUuid;

    public EconomableFaction(String factionUuid) {
        this.factionUuid = factionUuid;
    }

    @Override
    public String getUniqueIdentifier() {
        return "faction:" + factionUuid;
    }
}
