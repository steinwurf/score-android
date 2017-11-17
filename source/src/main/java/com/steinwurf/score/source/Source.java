package com.steinwurf.score.source;

/**
 * <h1>Handle to a native Score Source</h1>
 * Source wraps a native source object and gives access to its data members.
 */
@SuppressWarnings("JniMissingFunction")
public class Source
{
    static
    {
        // Load the native source library
        System.loadLibrary("source_jni");
    }

    public static final int MAX_SYMBOL_SIZE = 2000;
    public static final int MAX_GENERATION_SIZE = 500;

    public static class InvalidSnackPacketException extends Exception {
        public InvalidSnackPacketException(String message) {
            super(message);
        }
    }

    /**
     * A long representing a pointer to the underlying native object.
     */
    private final long pointer;

    /**
     * Construct Source object.
     */
    public Source()
    {
        pointer = init();
    }

    /**
     * Construct a native Score source and returns a long value which represents
     * the pointer of the created object.
     */
    private static native long init();

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
    public native void readMessage(byte[] buffer, int offset, int size);

    /**
     * Fills up the current input generation with zeros, and makes sure
     * that no partial symbol data is trapped in the source.
     * Should only be used at the end of a stream or before a long idle
     * period.
     */
    public native void flush();

    /**
     * Returns if the source has any data packets at the moment.
     * To check that a source is totally empty, this function should return
     * false after a flush() call has been issued.
     * @return true if the source has data packets
     */
    public native boolean hasDataPacket();

    /**
     * Returns the number of data packets available in the source.
     * @return the number of data packets
     */
    public native int dataPackets();

    /**
     * Returns a data packet from the source that should be transmitted to the sink.
     * @return the outgoing data message
     */
    public native byte[] getDataPacket();

    /**
     * Processes an incoming snack message from the sink. The internal
     * state of the source might change and it might schedule data packets
     * that are missing in the sink.
     * @param buffer the buffer containing the snack message
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
     */
    public native void readSnackPacket(byte[] buffer, int offset, int size) throws InvalidSnackPacketException;

    /**
     * Set the symbol size.
     * Has no effect on current active encoders.
     * @param size the symbols size in bytes
     */
    public native void setSymbolSize(int size);

    /**
     * Set the generation size.
     * Has no effect on current active encoders
     * @param symbols the number of symbols in the next created generation
     */
    public native void setGenerationSize(int symbols);

    /**
     * Set the amount of supported generations 'back in time'.
     * @param generations the number of old generations
     */
    public native void setGenerationWindowSize(int generations);

    /**
     * Set the auto-redundancy factor.
     * If called when a generation is being transmitted, this change will
     * first be active for the next generation.
     * @param redundancy the redundancy factor, must be non-negative.
     * 0.0  =  0% redundancy added to original data
     * 0.5  = 50% redundancy added to original data
     * 0.99 = 99% redundancy added to original data
     * Recommended value is between 0.0 and 0.5
     */
    public native void setDataRedundancy(float redundancy);

    /**
     * Set the feedback probability
     * @param probability the feedback probability between 0.0 and 1.0
     */
    public native void setFeedbackProbability(float probability);

    /**
     * Returns the amount of supported generations 'back in time'.
     * @return the amount of supported generations 'back in time'.
     */
    public native int generationWindowSize();

    /**
     * Return the auto-redundancy factor.
     * @return the auto-redundancy factor.
     */
    public native float dataRedundancy();

    /**
     * Return the feedback probability.
     * @return the feedback probability.
     */
    public native float feedbackProbability();

    /**
     * Return the symbol size of the encoder factory.
     * @return the symbol size of the encoder factory.
     */
    public native int symbolSize();

    /**
     * Return the generation size of the encoder factory.
     * @return the generation size of the encoder factory.
     */
    public native int generationSize();

    /**
     * Finalizes the object and it's underlying native part.
     */
    @Override
    protected void finalize() throws Throwable
    {
        finalize(pointer);
        super.finalize();
    }

    /**
     * Finalizes the underlying native part.
     * @param pointer A long representing a pointer to the underlying native object.
     */
    private native void finalize(long pointer);
}
