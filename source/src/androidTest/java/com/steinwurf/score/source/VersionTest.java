package com.steinwurf.score.source;

import junit.framework.TestCase;

public class VersionTest extends TestCase {
    public void testGet() throws Exception {
        assertFalse(Version.get().isEmpty());
    }
}