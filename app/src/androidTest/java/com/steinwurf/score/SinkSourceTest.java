package com.steinwurf.score;

import com.steinwurf.score.sink.Sink;
import com.steinwurf.score.source.InvalidSnackPacketException;
import com.steinwurf.score.source.ManualSource;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

public class SinkSourceTest {

    @Test
    public void testInputOutput() throws Sink.InvalidDataPacketException {

        ManualSource source = new ManualSource(ManualSource.Profile.STREAM);
        source.setSymbolSize(100);
        source.setFeedbackProbability(0.0f);
        source.setDataRedundancy(0.5f);

        int messageSize = 256;
        byte[] input = new byte[messageSize];

        Random random = new Random();
        random.nextBytes(input);

        // Feed the input to the source
        source.readMessage(input);
        source.flush();

        byte[] output = new byte[messageSize];

        boolean complete = false;
        boolean first = true;

        Sink sink = new Sink();

        // See if there is any data in the source
        Assert.assertTrue(source.hasDataPacket());
        while (source.hasDataPacket()) {
            byte[] dataPacket = source.getDataPacket();
            Assert.assertThat(dataPacket.length, Matchers.anyOf(
                    Matchers.equalTo(18),    // Flush
                    Matchers.equalTo(118),   // Systematic
                    Matchers.equalTo(120))); // Coded

            // Make sure the data packet is not all zeros
            Assert.assertFalse(Arrays.equals(new byte[dataPacket.length], dataPacket));

            // Drop the first packet
            if (first)
            {
                first = false;
                continue;
            }

            // receive the data packet
            sink.readDataPacket(dataPacket);

            // is the receiver ready to output data
            while (sink.hasData()) {

                Assert.assertEquals(output.length, sink.messageSize());

                sink.writeToMessage(output);
                if (sink.messageCompleted()) {
                    // Compare original and result
                    Assert.assertArrayEquals(input, output);
                    complete = true;
                }
            }

            // feedback probability is 0
            Assert.assertFalse(sink.hasSnackPacket());
        }

        Assert.assertTrue(complete);
    }
}
