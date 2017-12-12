package com.steinwurf.score.source;

public abstract class Source {

    public static final int MAX_SYMBOL_SIZE = 2000;
    public static final int MAX_GENERATION_SIZE = 500;

    /**
     * Reads a complete message to the bitstream.
     * @param buffer The buffer containing message to read.
     */
    public void readMessage(byte[] buffer)
    {
        readMessage(buffer, 0, buffer.length);
    }

    /**
     * Reads a complete message to the bitstream.
     * @param buffer The buffer containing the message to read.
     * @param offset the offset of the buffer
     * @param size the size of the buffer
     */
    public abstract void readMessage(byte[] buffer, int offset, int size);

    /**
     * Fills up the current input generation with zeros, and makes sure
     * that no partial symbol data is trapped in the source.
     * Should only be used at the end of a stream or before a long idle
     * period.
     */
    public abstract void flush();

    /**
     * Returns if the source has any data packets at the moment.
     * To check that a source is totally empty, this function should return
     * false after a flush() call has been issued.
     * @return true if the source has data packets
     */
    public abstract boolean hasDataPacket();

    /**
     * Returns the number of data packets available in the source.
     * @return the number of data packets
     */
    public abstract int dataPackets();

    /**
     * Returns a data packet from the source that should be transmitted to the sink.
     * @return the outgoing data message
     * @throws IllegalStateException If no data packet is available.
     * Use {@link Source#hasDataPacket()} to check.
     */
    public final byte[] getDataPacket() throws IllegalStateException
    {
        if (!hasDataPacket())
            throw new IllegalStateException("No data packet available.");
        return nativeGetDataPacket();
    }
    abstract byte[] nativeGetDataPacket();

    /**
     * Processes an incoming snack message from the sink. The internal
     * state of the source might change and it might schedule data packets
     * that are missing in the sink.
     * @param buffer the buffer containing the snack message
     * @throws InvalidSnackPacketException if Snack Packet was invalid
     */
    public void readSnackPacket(byte[] buffer) throws InvalidSnackPacketException
    {
        readSnackPacket(buffer, 0, buffer.length);
    }

    /**
     * Processes an incoming snack message from the sink. The internal
     * state of the source might change and it might schedule data packets
     * that are missing in the sink.
     * @param buffer the buffer containing the feedback message
     * @param offset the offset of the buffer
     * @param size the size of the buffer
     * @throws InvalidSnackPacketException if Snack Packet was invalid
     */
    public abstract void readSnackPacket(byte[] buffer, int offset, int size) throws InvalidSnackPacketException;

    /**
     * Returns the amount of supported generations 'back in time'.
     * @return the amount of supported generations 'back in time'.
     */
    public abstract int generationWindowSize();

    /**
     * Return the auto-redundancy factor.
     * @return the auto-redundancy factor.
     */
    public abstract float dataRedundancy();

    /**
     * Return the feedback probability.
     * @return the feedback probability.
     */
    public abstract float feedbackProbability();

    /**
     * Return the symbol size of the encoder factory.
     * @return the symbol size of the encoder factory.
     */
    public abstract int symbolSize();

    /**
     * Return the generation size of the encoder factory.
     * @return the generation size of the encoder factory.
     */
    public abstract int generationSize();
}
