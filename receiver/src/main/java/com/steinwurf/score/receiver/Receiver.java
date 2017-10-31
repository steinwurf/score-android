package com.steinwurf.score.receiver;

/**
 * <h1>Handle to a native Score Receiver</h1>
 * Receiver wraps a native receiver object and gives access to its data members.
 */
@SuppressWarnings("JniMissingFunction")
public class Receiver
{
    static
    {
        // Load the native receiver library
        System.loadLibrary("receiver_jni");
    }
    /**
     * A long representing a pointer to the underlying native object.
     */
    private final long pointer;

    /**
     * Construct Receiver object.
     */
    public Receiver()
    {
        pointer = init();
    }

    /**
     * Construct a native Score receiver and returns a long value which represents
     * the pointer of the created object.
     */
    private static native long init();

    /**
     * Returns if the receiver has any outgoing messages at the moment.
     * @return true if the receiver has outgoing messages
     */

    public native boolean hasOutgoingMessage();
    /**
     * Returns the number of outgoing messages available in the receiver.
     * @return the number of outgoing messages
     */

    public native int outgoingMessages();
    /**
     * Returns an outgoing feedback message from the receiver that should be
     * transmitted to the sender.
     * @return the outgoing feedback message
     */

    public native byte[] getOutgoingMessage();
    /**
     * Processes an incoming data message from the sender. The internal
     * state of the receiver might change: it can decode some original messages
     * and it might schedule additional feedback messages.
     * @param buffer the buffer containing the data message
     */

    public native void receiveMessage(byte[] buffer);
    /**
     * Returns true if the receiver has decoded any original messages that can be
     * extracted with {@link #getData()}.
     * @return true if the receiver has any original messages
     */

    public native boolean dataAvailable();
    /**
     * Returns an original atomic message that was added to the sender.
     * The in-order delivery of the messages is guaranteed,
     * but some messages might be lost.
     * @return the decoded original message
     */
    public native byte[] getData();

    /**
     * Finalizes the underlying native part.
     * @param pointer A long representing a pointer to the underlying native object.
     */
    private native void finalize(long pointer);
}