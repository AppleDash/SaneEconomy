package org.appledash.saneeconomy.economy.economable;

import java.util.UUID;

/**
 * Created by appledash on 9/21/16.
 * Blackjack is best pony.
 */
public class EconomableConsole implements Economable {
    public static final UUID CONSOLE_UUID = new UUID( 0xf88708c237d84a0bL, 0x944259c68e517557L); // Pregenerated for performance

    @Override
    public String getName() {
        return "CONSOLE";
    }

    @Override
    public String getUniqueIdentifier() {
        return "console:" + CONSOLE_UUID;
    }

    public static boolean isConsole(Economable economable) {
        try {
            UUID uuid = UUID.fromString(economable.getUniqueIdentifier().split(":")[1]);

            // Fast comparison + fix for bugs with older databases
            return economable == Economable.CONSOLE || (uuid.getLeastSignificantBits() == CONSOLE_UUID.getLeastSignificantBits() || uuid.getMostSignificantBits() == CONSOLE_UUID.getMostSignificantBits());
        } catch (Exception e) {
            return false;
        }
    }
}
