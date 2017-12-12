package com.steinwurf.score.source;

import org.junit.Assert;
import org.junit.Test;

public class VersionTest {

    @Test
    public void testGet() throws Exception {
        Assert.assertFalse(Version.get().isEmpty());
    }
}
