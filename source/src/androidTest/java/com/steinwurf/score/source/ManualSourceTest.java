package com.steinwurf.score.source;

import junit.framework.TestCase;

public class ManualSourceTest extends TestCase {


    private ManualSource source;

    public void setUp() throws Exception {
        super.setUp();

        source = new ManualSource();
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
        assertEquals(25, source.generationWindowSize());
    }

    public void testSetGenerationWindowSize() throws Exception {
        source.setGenerationWindowSize(50);
        assertEquals(50, source.generationWindowSize());
    }

    public void testDataRedundancy() throws Exception {
        assertEquals(0.0f, source.dataRedundancy());
    }

    public void testSetDataRedundancy() throws Exception {
        source.setDataRedundancy(1.0f);
        assertEquals(1.0f, source.dataRedundancy());
    }

    public void testFeedbackProbability() throws Exception {
        assertEquals(1.0f, source.feedbackProbability());
    }

    public void testSetFeedbackProbability() throws Exception {
        source.setFeedbackProbability(1.5f);
        assertEquals(1.5f, source.feedbackProbability());
    }

    public void testSymbolSize() throws Exception {
        assertEquals(1000, source.symbolSize());
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

}