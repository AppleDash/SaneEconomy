package org.appledash.saneeconomy.economy;

import org.bukkit.configuration.file.FileConfiguration;

import java.text.DecimalFormat;

/**
 * Created by AppleDash on 6/13/2016.
 * Blackjack is still best pony.
 *
 * Represents an in-game currency.
 */
public class Currency {
    private final String nameSingular;
    private final String namePlural;
    private final DecimalFormat format;

    private Currency(String nameSingular, String namePlural, DecimalFormat format) {
        this.nameSingular = nameSingular;
        this.namePlural = namePlural;
        this.format = format;
    }

    public static Currency fromConfig(FileConfiguration config, String baseNode) {
        return new Currency(
                config.getString(String.format("%s.name.singular", baseNode), "dollar"),
                config.getString(String.format("%s.name.plural", baseNode), "dollars"),
                new DecimalFormat(config.getString(String.format("%s.format", baseNode), "0.00"))
        );
    }

    /**
     * Format a money amount with this currency's format.
     * @param amount Money amount.
     * @return Formatted amount string.
     */
    public String formatAmount(double amount) {
        if (amount == 1) {
            return String.format("%s %s", format.format(amount), nameSingular);
        }

        return String.format("%s %s", format.format(amount), namePlural);
    }

    /**
     * Get this currency's singular name.
     * Example: "Dollar"
     * @return Singular name.
     */
    public String getSingularName() {
        return nameSingular;
    }

    /**
     * Get this currency's plural name.
     * Example: "Dollars"
     * @return Plural name.
     */
    public String getPluralName() {
        return namePlural;
    }

    /**
     * Get this currency's number format.
     * @return DecimalFormat instance
     */
    public DecimalFormat getFormat() {
        return format;
    }
}
