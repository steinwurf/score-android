package com.steinwurf.score.source;

import junit.framework.TestCase;

public class AutoSourceTest extends TestCase {

    private AutoSource source;

    public void setUp() throws Exception {
        super.setUp();

        source = new AutoSource();
    }

    public void testFlush() throws Exception {
        assertFalse(source.hasDataPacket());
        source.flush();
        assertTrue(source.hasDataPacket());
    }

    public void testHasDataPacket() throws Exception {
        assertFalse(source.hasDataPacket());
    }

    public void testDataPackets() throws Exception {
        assertEquals(0, source.dataPackets());
    }

    public void testGenerationWindowSize() throws Exception {
        assertEquals(0, source.generationWindowSize());
    }

    public void testDataRedundancy() throws Exception {
        assertEquals(0.0f, source.dataRedundancy());
    }

    public void testFeedbackProbability() throws Exception {
        assertEquals(1.0f, source.feedbackProbability());
    }

    public void testSymbolSize() throws Exception {
        assertEquals(1400, source.symbolSize());
    }

    public void testSetSymbolSize() throws Exception {
        source.setSymbolSize(2000);
        assertEquals(2000, source.symbolSize());
    }

    public void testGenerationSize() throws Exception {
        assertEquals(10, source.generationSize());
    }

    public void testSetGenerationSize() throws Exception {
        source.setGenerationSize(500);
        assertEquals(500, source.generationSize());
    }

    public void testMaxDataRedundancy() throws Exception {
        assertEquals(2.0f, source.maxDataRedundancy());
    }

    public void testSetMaxDataRedundancy() throws Exception {
        source.setMaxDataRedundancy(1);
        assertEquals(1.0f, source.maxDataRedundancy());
    }

    public void testDataRedundancyEstimationGain() throws Exception {
        assertEquals(0.25f, source.dataRedundancyEstimationGain());
    }

    public void testSetDataRedundancyEstimationGain() throws Exception {
        source.setDataRedundancyEstimationGain(0.5f);
        assertEquals(0.5f, source.dataRedundancyEstimationGain());
    }

    public void testTargetSnacksPerGeneration() throws Exception {
        assertEquals(5, source.targetSnacksPerGeneration());
    }

    public void testSetTargetSnacksPerGeneration() throws Exception {
        source.setTargetSnacksPerGeneration(3);
        assertEquals(3, source.targetSnacksPerGeneration());
    }

    public void testMinFeedbackProbability() throws Exception {
        assertEquals(0.0f, source.minFeedbackProbability());
    }

    public void testSetMinFeedbackProbability() throws Exception {
        source.setMinFeedbackProbability(0.25f);
        assertEquals(0.25f, source.minFeedbackProbability());
    }

    public void testMaxFeedbackProbability() throws Exception {
        assertEquals(1.0f, source.maxFeedbackProbability());
    }

    public void testSetMaxFeedbackProbability() throws Exception {
        source.setMaxFeedbackProbability(0.5f);
        assertEquals(0.5f, source.maxFeedbackProbability());
    }

    public void testFeedbackProbabilityGain() throws Exception {
        assertEquals(0.1f, source.feedbackProbabilityGain());
    }

    public void testSetFeedbackProbabilityGain() throws Exception {
        source.setFeedbackProbabilityGain(0.6f);
        assertEquals(0.6f, source.feedbackProbabilityGain());
    }

    public void testTargetRepairDelay() throws Exception {
        assertEquals(300, source.targetRepairDelay());
    }

    public void testSetTargetRepairDelay() throws Exception {
        source.setTargetRepairDelay(137);
        assertEquals(137, source.targetRepairDelay());
    }

}