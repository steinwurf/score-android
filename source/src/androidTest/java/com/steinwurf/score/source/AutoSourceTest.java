package com.steinwurf.score.source;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

public class AutoSourceTest {

    private AutoSource source;

    @Before
    public void setUp() {

        source = new AutoSource();
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
        Assert.assertEquals(0, source.generationWindowSize());
    }

    @Test
    public void testDataRedundancy() {
        Assert.assertEquals(0.0f, source.dataRedundancy(), 0.0);
    }

    @Test
    public void testFeedbackProbability() {
        Assert.assertEquals(1.0f, source.feedbackProbability(), 0.0);
    }

    @Test
    public void testSymbolSize() {
        Assert.assertEquals(1400, source.symbolSize());
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
    public void testMaxDataRedundancy() {
        Assert.assertEquals(2.0f, source.maxDataRedundancy(), 0.0);
    }

    @Test
    public void testSetMaxDataRedundancy() {
        source.setMaxDataRedundancy(1);
        Assert.assertEquals(1.0f, source.maxDataRedundancy(), 0.0);
    }

    @Test
    public void testDataRedundancyEstimationGain() {
        Assert.assertEquals(0.25f, source.dataRedundancyEstimationGain(), 0.0);
    }

    @Test
    public void testSetDataRedundancyEstimationGain() {
        source.setDataRedundancyEstimationGain(0.5f);
        Assert.assertEquals(0.5f, source.dataRedundancyEstimationGain(), 0.0);
    }

    @Test
    public void testTargetSnacksPerGeneration() {
        Assert.assertEquals(5, source.targetSnacksPerGeneration());
    }

    @Test
    public void testSetTargetSnacksPerGeneration() {
        source.setTargetSnacksPerGeneration(3);
        Assert.assertEquals(3, source.targetSnacksPerGeneration(), 0.0);
    }

    @Test
    public void testMinFeedbackProbability() {
        Assert.assertEquals(0.0f, source.minFeedbackProbability(), 0.0);
    }

    @Test
    public void testSetMinFeedbackProbability() {
        source.setMinFeedbackProbability(0.25f);
        Assert.assertEquals(0.25f, source.minFeedbackProbability(), 0.0);
    }

    @Test
    public void testMaxFeedbackProbability() {
        Assert.assertEquals(1.0f, source.maxFeedbackProbability(), 0.0);
    }

    @Test
    public void testSetMaxFeedbackProbability() {
        source.setMaxFeedbackProbability(0.5f);
        Assert.assertEquals(0.5f, source.maxFeedbackProbability(), 0.0);
    }

    @Test
    public void testFeedbackProbabilityGain() {
        Assert.assertEquals(0.1f, source.feedbackProbabilityGain(), 0.0);
    }

    @Test
    public void testSetFeedbackProbabilityGain() {
        source.setFeedbackProbabilityGain(0.6f);
        Assert.assertEquals(0.6f, source.feedbackProbabilityGain(), 0.0);
    }

    @Test
    public void testTargetRepairDelay() {
        Assert.assertEquals(300, source.targetRepairDelay());
    }

    @Test
    public void testSetTargetRepairDelay() {
        source.setTargetRepairDelay(137);
        Assert.assertEquals(137, source.targetRepairDelay());
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
