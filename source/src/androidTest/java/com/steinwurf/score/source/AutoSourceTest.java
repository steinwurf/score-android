package com.steinwurf.score.source;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AutoSourceTest {

    private AutoSource source;

    @Before
    public void setUp() throws Exception {

        source = new AutoSource();
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
        Assert.assertEquals(0, source.generationWindowSize());
    }

    @Test
    public void testDataRedundancy() throws Exception {
        Assert.assertEquals(0.0f, source.dataRedundancy(), 0.0);
    }

    @Test
    public void testFeedbackProbability() throws Exception {
        Assert.assertEquals(1.0f, source.feedbackProbability(), 0.0);
    }

    @Test
    public void testSymbolSize() throws Exception {
        Assert.assertEquals(1400, source.symbolSize());
    }

    @Test
    public void testSetSymbolSize() throws Exception {
        source.setSymbolSize(2051);
        Assert.assertEquals(2051, source.symbolSize());
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

    @Test
    public void testMaxDataRedundancy() throws Exception {
        Assert.assertEquals(2.0f, source.maxDataRedundancy(), 0.0);
    }

    @Test
    public void testSetMaxDataRedundancy() throws Exception {
        source.setMaxDataRedundancy(1);
        Assert.assertEquals(1.0f, source.maxDataRedundancy(), 0.0);
    }

    @Test
    public void testDataRedundancyEstimationGain() throws Exception {
        Assert.assertEquals(0.25f, source.dataRedundancyEstimationGain(), 0.0);
    }

    @Test
    public void testSetDataRedundancyEstimationGain() throws Exception {
        source.setDataRedundancyEstimationGain(0.5f);
        Assert.assertEquals(0.5f, source.dataRedundancyEstimationGain(), 0.0);
    }

    @Test
    public void testTargetSnacksPerGeneration() throws Exception {
        Assert.assertEquals(5, source.targetSnacksPerGeneration());
    }

    @Test
    public void testSetTargetSnacksPerGeneration() throws Exception {
        source.setTargetSnacksPerGeneration(3);
        Assert.assertEquals(3, source.targetSnacksPerGeneration(), 0.0);
    }

    @Test
    public void testMinFeedbackProbability() throws Exception {
        Assert.assertEquals(0.0f, source.minFeedbackProbability(), 0.0);
    }

    @Test
    public void testSetMinFeedbackProbability() throws Exception {
        source.setMinFeedbackProbability(0.25f);
        Assert.assertEquals(0.25f, source.minFeedbackProbability(), 0.0);
    }

    @Test
    public void testMaxFeedbackProbability() throws Exception {
        Assert.assertEquals(1.0f, source.maxFeedbackProbability(), 0.0);
    }

    @Test
    public void testSetMaxFeedbackProbability() throws Exception {
        source.setMaxFeedbackProbability(0.5f);
        Assert.assertEquals(0.5f, source.maxFeedbackProbability(), 0.0);
    }

    @Test
    public void testFeedbackProbabilityGain() throws Exception {
        Assert.assertEquals(0.1f, source.feedbackProbabilityGain(), 0.0);
    }

    @Test
    public void testSetFeedbackProbabilityGain() throws Exception {
        source.setFeedbackProbabilityGain(0.6f);
        Assert.assertEquals(0.6f, source.feedbackProbabilityGain(), 0.0);
    }

    @Test
    public void testTargetRepairDelay() throws Exception {
        Assert.assertEquals(300, source.targetRepairDelay());
    }

    @Test
    public void testSetTargetRepairDelay() throws Exception {
        source.setTargetRepairDelay(137);
        Assert.assertEquals(137, source.targetRepairDelay());
    }
}
