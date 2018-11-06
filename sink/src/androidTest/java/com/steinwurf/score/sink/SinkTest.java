package com.steinwurf.score.sink;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SinkTest {

    private Sink sink;

    @Before
    public void setUp() {
        sink = new Sink();
    }

    @Test
    public void testHasSnackPacket() {
        Assert.assertFalse(sink.hasSnackPacket());
    }

    @Test
    public void testSnackPackets() {
        Assert.assertFalse(sink.hasSnackPacket());
    }

    @Test(expected = IllegalStateException.class)
    public void testGetSnackPacket() {
        Assert.assertFalse(sink.hasSnackPacket());
        sink.getSnackPacket();
    }

    @Test
    public void testHasMessage() {
        Assert.assertFalse(sink.hasData());
    }

    @Test(expected = IllegalStateException.class)
    public void testMessageSize() {
        Assert.assertFalse(sink.hasData());
        sink.messageSize();
    }

    @Test(expected = IllegalStateException.class)
    public void testGetMessage() {
        Assert.assertFalse(sink.hasData());
        byte[] buffer = new byte[10];
        sink.writeToMessage(buffer);
    }
}
