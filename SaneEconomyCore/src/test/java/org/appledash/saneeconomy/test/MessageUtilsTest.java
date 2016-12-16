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

    @Test(expected = IllegalArgumentException.class)
    public void testBadIndexedFormat() {
         MessageUtils.indexedFormat("Hello, {3}!", "world", "something");
    }
}
