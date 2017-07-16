package org.appledash.saneeconomy.test;

import org.appledash.saneeconomy.updates.VersionComparer;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by appledash on 7/15/17.
 * Blackjack is best pony.
 */
public class VersionComparerTest {
    @Test
    public void testVersionComparer() {
        Assert.assertTrue(VersionComparer.isSemVerGreaterThan("0.12.6", "1.0.0"));
        Assert.assertFalse(VersionComparer.isSemVerGreaterThan("2.0.0", "1.0.0"));
        Assert.assertTrue(VersionComparer.isSemVerGreaterThan("0.1.0", "0.2.0"));
        Assert.assertTrue(VersionComparer.isSemVerGreaterThan("1.0.0", "2.0.0"));
        Assert.assertFalse(VersionComparer.isSemVerGreaterThan("0.12.6", "0.5.7"));
    }
}
