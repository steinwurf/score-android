package com.steinwurf.score.source;

import org.junit.Assert;
import org.junit.Test;

public class VersionTest {

    @Test
    public void testGet() {
        Assert.assertFalse(Version.get().isEmpty());
    }
}
