package com.steinwurf.score.source;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

public class ManualSourceTest {


    private ManualSource source;

    @Before
    public void setUp() {

        source = new ManualSource();
    }

    @Test(expected = IllegalStateException.class)
    public void testGetDataPacket() {
        Assert.assertFalse(source.hasDataPacket());
        source.getDataPacket();
    }

    @Test
    public void testHasDataPacket() {
        Assert.assertFalse(source.hasDataPacket());
    }

    @Test
    public void testGenerationWindowSize() {
        Assert.assertEquals(25, source.generationWindowSize());
    }

    @Test
    public void testSetGenerationWindowSize() {
        source.setGenerationWindowSize(50);
        Assert.assertEquals(50, source.generationWindowSize());
    }

    @Test
    public void testDataRedundancy() {
        Assert.assertEquals(0.0f, source.dataRedundancy(), 0.0);
    }

    @Test
    public void testSetDataRedundancy() {
        source.setDataRedundancy(1.0f);
        Assert.assertEquals(1.0f, source.dataRedundancy(), 0.0);
    }

    @Test
    public void testFeedbackProbability() {
        Assert.assertEquals(1.0f, source.feedbackProbability(), 0.0);
    }

    @Test
    public void testSetFeedbackProbability() {
        source.setFeedbackProbability(1.5f);
        Assert.assertEquals(1.5f, source.feedbackProbability(), 0.0);
    }

    @Test
    public void testSymbolSize() {
        Assert.assertEquals(1000, source.symbolSize());
    }

    @Test
    public void testSetSymbolSize() {
        source.setSymbolSize(2000);
        Assert.assertEquals(2000, source.symbolSize());
    }

    @Test
    public void testGenerationSize() {
        Assert.assertEquals(10, source.generationSize());
    }

    @Test
    public void testSetGenerationSize() {
        source.setGenerationSize(500);
        Assert.assertEquals(500, source.generationSize());
    }

    @Test
    public void testReadMessageGetDataPacket() {
        byte[] message = new byte[2000];
        new Random().nextBytes(message);
        Assert.assertFalse(source.hasDataPacket());
        source.readMessage(message);
        Assert.assertTrue(source.hasDataPacket());
        while (source.hasDataPacket())
        {
            byte[] dataPacket = source.getDataPacket();
            Assert.assertTrue(dataPacket.length > 1000);
        }
        source.flush();
        Assert.assertTrue(source.hasDataPacket());
        while (source.hasDataPacket())
        {
            byte[] dataPacket = source.getDataPacket();
            Assert.assertTrue(dataPacket.length > 10);
        }
    }
}
