package org.appledash.saneeconomy.test;

import org.appledash.saneeconomy.utils.MessageUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by appledash on 12/15/16.
 * Blackjack is best pony.
 */
public class MessageUtilsTest {
    @Test
    public void testIndexedFormat() {
        Assert.assertEquals("Hello, world!", MessageUtils.indexedFormat("{1}, {2}!", "Hello", "world"));
        Assert.assertEquals("Hello, world!", MessageUtils.indexedFormat("Hello, {1}!", "world", "discarded"));
        Assert.assertEquals("Hello, world!", MessageUtils.indexedFormat("Hello, {2}!", "discarded", "world"));
        Assert.assertEquals("Hello, world!", MessageUtils.indexedFormat("Hello, world!", "this", "shouldn't", "change"));
    }

    @Test
    public void testAdvancedIndexFormat() {
        Assert.assertEquals("Temperature: 20.01 degrees", MessageUtils.indexedFormat("Temperature: {1:.2f} degrees", 20.01f));
        Assert.assertEquals("Index: 01", MessageUtils.indexedFormat("Index: {1:02d}", 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadIndexedFormat() {
         MessageUtils.indexedFormat("Hello, {3}!", "world", "something");
    }
}
