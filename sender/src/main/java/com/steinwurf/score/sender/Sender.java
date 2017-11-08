package com.steinwurf.score.sender;

/**
 * <h1>Handle to a native Score Sender</h1>
 * Sender wraps a native sender object and gives access to its data members.
 */
@SuppressWarnings("JniMissingFunction")
public class Sender
{
    static
    {
        // Load the native sender library
        System.loadLibrary("sender_jni");
    }

    public static final int MAX_SYMBOL_SIZE = 2000;
    public static final int MAX_GENERATION_SIZE = 500;

    public static class InvalidFeedbackMessageException extends Exception {
        public InvalidFeedbackMessageException (String message) {
            super(message);
        }
    }

    /**
     * A long representing a pointer to the underlying native object.
     */
    private final long pointer;

    /**
     * Construct Sender object.
     */
    public Sender()
    {
        pointer = init();
    }

    /**
     * Construct a native Score sender and returns a long value which represents
     * the pointer of the created object.
     */
    private static native long init();

    /**
     * Writes a complete atomic message to the bitstream.
     * @param buffer The buffer containing message to write to the bitstream.
     */
    public void writeData(byte[] buffer)
    {
        writeData(buffer, 0, buffer.length);
    }

    /**
     * Writes a complete atomic message to the bitstream.
     * @param buffer The buffer containing the message to write to the bitstream.
     * @param offset the offset of the buffer
     * @param size the size of the buffer
     */
    public native void writeData(byte[] buffer, int offset, int size);

    /**
     * Fills up the current input generation with zeros, and makes sure
     * that no partial symbol data is trapped in the encoder.
     * Should only be used at the end of a stream or before a long idle
     * period.
     */
    public native void flush();

    /**
     * Returns if the sender has any outgoing messages at the moment.
     * To check that a sender is totally empty, this function should return
     * false after a flush() call has been issued.
     * @return true if the sender has outgoing messages
     */
    public native boolean hasOutgoingMessage();

    /**
     * Returns the number of outgoing messages available in the sender.
     * @return the number of outgoing messages
     */
    public native int outgoingMessages();

    /**
     * Returns an outgoing data message from the sender that should be
     * transmitted to the receiver.
     * @return the outgoing data message
     */
    public native byte[] getOutgoingMessage();

    /**
     * Processes an incoming feedback message from the receiver. The internal
     * state of the sender might change and it might schedule data messages
     * that are missing on the receiver.
     * @param buffer the buffer containing the feedback message
     */
    public void receiveMessage(byte[] buffer) throws InvalidFeedbackMessageException
    {
        receiveMessage(buffer, 0, buffer.length);
    }

    /**
     * Processes an incoming feedback message from the receiver. The internal
     * state of the sender might change and it might schedule data messages
     * that are missing on the receiver.
     * @param buffer the buffer containing the feedback message
     * @param offset the offset of the buffer
     * @param size the size of the buffer
     */
    public native void receiveMessage(byte[] buffer, int offset, int size) throws InvalidFeedbackMessageException;

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
