package com.steinwurf.score.sink;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SinkTest {

    private Sink sink;

    @Before
    public void setUp() throws Exception {
        sink = new Sink();
    }

    @Test
    public void testHasSnackPacket() throws Exception {
        Assert.assertFalse(sink.hasSnackPacket());
    }

    @Test
    public void testSnackPackets() throws Exception {
        Assert.assertEquals(0, sink.snackPackets());
    }

    @Test(expected = IllegalStateException.class)
    public void testGetSnackPacket() throws Exception {
        Assert.assertFalse(sink.hasSnackPacket());
        sink.getSnackPacket();
    }

    @Test
    public void testHasMessage() throws Exception {
        Assert.assertFalse(sink.hasMessage());
    }

    @Test(expected = IllegalStateException.class)
    public void testGetMessage() throws Exception {
        Assert.assertFalse(sink.hasMessage());
        sink.getMessage();
    }
}
