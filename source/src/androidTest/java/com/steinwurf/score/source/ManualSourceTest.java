package com.steinwurf.score.source;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ManualSourceTest {


    private ManualSource source;

    @Before
    public void setUp() throws Exception {

        source = new ManualSource();
    }

    @Test(expected = IllegalStateException.class)
    public void testGetDataPacket() throws Exception {
        Assert.assertFalse(source.hasDataPacket());
        source.getDataPacket();
    }

    @Test
    public void testFlush() throws Exception {
        Assert.assertFalse(source.hasDataPacket());
        source.flush();
        Assert.assertTrue(source.hasDataPacket());
    }

    @Test
    public void testHasDataPacket() throws Exception {
        Assert.assertFalse(source.hasDataPacket());
    }

    @Test
    public void testDataPackets() throws Exception {
        Assert.assertEquals(0, source.dataPackets());
    }

    @Test
    public void testGenerationWindowSize() throws Exception {
        Assert.assertEquals(25, source.generationWindowSize());
    }

    @Test
    public void testSetGenerationWindowSize() throws Exception {
        source.setGenerationWindowSize(50);
        Assert.assertEquals(50, source.generationWindowSize());
    }

    @Test
    public void testDataRedundancy() throws Exception {
        Assert.assertEquals(0.0f, source.dataRedundancy(), 0.0);
    }

    @Test
    public void testSetDataRedundancy() throws Exception {
        source.setDataRedundancy(1.0f);
        Assert.assertEquals(1.0f, source.dataRedundancy(), 0.0);
    }

    @Test
    public void testFeedbackProbability() throws Exception {
        Assert.assertEquals(1.0f, source.feedbackProbability(), 0.0);
    }

    @Test
    public void testSetFeedbackProbability() throws Exception {
        source.setFeedbackProbability(1.5f);
        Assert.assertEquals(1.5f, source.feedbackProbability(), 0.0);
    }

    @Test
    public void testSymbolSize() throws Exception {
        Assert.assertEquals(1000, source.symbolSize());
    }

    @Test
    public void testSetSymbolSize() throws Exception {
        source.setSymbolSize(2000);
        Assert.assertEquals(2000, source.symbolSize());
    }

    @Test
    public void testGenerationSize() throws Exception {
        Assert.assertEquals(10, source.generationSize());
    }

    @Test
    public void testSetGenerationSize() throws Exception {
        source.setGenerationSize(500);
        Assert.assertEquals(500, source.generationSize());
    }

}