package org.appledash.saneeconomy.economy;

import com.google.common.base.Strings;
import org.appledash.sanelib.messages.MessageUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

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
    private final String balanceFormat;

    public Currency(String nameSingular, String namePlural, DecimalFormat format) {
        this(nameSingular, namePlural, format, "{1} {2}");
    }

    public Currency(String nameSingular, String namePlural, DecimalFormat format, String balanceFormat) {
        this.nameSingular = nameSingular;
        this.namePlural = namePlural;
        this.format = format;
        this.balanceFormat = balanceFormat;
    }

    public static Currency fromConfig(ConfigurationSection config) {
        DecimalFormat format = new DecimalFormat(config.getString("format", "0.00"));

        if (config.getInt("grouping", 0) > 0) {
            DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
            if (symbols.getDecimalSeparator() == ',') { // French
                symbols.setGroupingSeparator(' ');
            } else {
                symbols.setGroupingSeparator(',');
            }

            String groupingSeparator = config.getString("grouping-separator", null);

            if (!Strings.isNullOrEmpty(groupingSeparator)) {
                symbols.setGroupingSeparator(groupingSeparator.charAt(0));
            }

            String decimalSeparator = config.getString("decimal-separator", ".");

            if (!Strings.isNullOrEmpty(decimalSeparator)) {
                symbols.setDecimalSeparator(decimalSeparator.charAt(0));
            }

            format.setDecimalFormatSymbols(symbols);
            format.setGroupingUsed(true);
            format.setGroupingSize(3);
        }

        return new Currency(
                config.getString("name.singular", "dollar"),
                config.getString("name.plural", "dollars"),
                format,
                config.getString("balance-format", "{1} {2}")
        );
    }

    /**
     * Format a money amount with this currency's format.
     * @param amount Money amount.
     * @return Formatted amount string.
     */
    public String formatAmount(double amount) {
        String formatted;
        if (amount == 1) {
            formatted = MessageUtils.indexedFormat(balanceFormat, format.format(amount), nameSingular);
        } else {
            formatted = MessageUtils.indexedFormat(balanceFormat, format.format(amount), namePlural);
        }

        return ChatColor.translateAlternateColorCodes('&', formatted);
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
