package org.appledash.saneeconomy.test;

import org.appledash.saneeconomy.economy.Currency;
import org.junit.Assert;
import org.junit.Test;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 * Created by AppleDash on 7/29/2016.
 * Blackjack is still best pony.
 */
public class CurrencyTest {
    @Test
    public void testCurrencyFormat() {
        DecimalFormatSymbols testSymbols = new DecimalFormatSymbols();
        char decimalSeparator=testSymbols.getDecimalSeparator();
        
        Currency currency = new Currency("test dollar", "test dollars", new DecimalFormat("0.00"));
        if(decimalSeparator == ',') { // French, Indonesian.
            Assert.assertEquals(currency.formatAmount(1.0D), "1,00 test dollar");
            Assert.assertEquals(currency.formatAmount(1337.0D), "1337,00 test dollars");
        } else {
            Assert.assertEquals(currency.formatAmount(1.0D), "1.00 test dollar");
            Assert.assertEquals(currency.formatAmount(1337.0D), "1337.00 test dollars");
        }
    }
}
