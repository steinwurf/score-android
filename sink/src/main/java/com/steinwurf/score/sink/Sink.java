package com.steinwurf.score.sink;
/*-
 * Copyright (c) 2017 Steinwurf ApS
 * All Rights Reserved
 *
 * THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF STEINWURF
 * The copyright notice above does not evidence any
 * actual or intended publication of such source code.
 */

/**
 * <h1>Handle to a native Score Sink</h1>
 * Sink wraps a native sink object and gives access to its data members.
 */
@SuppressWarnings("JniMissingFunction")
public class Sink
{
    static
    {
        // Load the native sink library
        System.loadLibrary("sink_jni");
    }

    public static class InvalidDataPacketException extends Exception {
        public InvalidDataPacketException(String message) {
            super(message);
        }
    }

    /**
     * A long representing a pointer to the underlying native object.
     */
    private final long pointer;

    /**
     * Construct Sink object.
     */
    public Sink()
    {
        pointer = init();
    }

    /**
     * Construct a native Score sink and returns a long value which represents
     * the pointer of the created object.
     */
    private static native long init();

    /**
     * Returns true if the sink has decoded any data that can be
     * extracted with {@link #writeToMessage(byte[])}.
     * @return true if the sink has any data available
     */
    public native boolean hasData();

    /**
     * Returns the size of the message
     * @return size of the message in bytes
     */
    public native int messageSize();

    /**
     * Write parts of a decoded message the given buffer.
     * If the parts written to the buffer is a complete message, {@link #messageCompleted()} will
     * return true.
     * @param buffer buffer to write the message parts to.
     * @param offset offset of the buffer.
     */
    public native void writeToMessage(byte[] buffer, int offset);

    /**
     * Write parts of a decoded message the given buffer.
     * If the parts written to the buffer is a complete message, {@link #messageCompleted()} will
     * return true.
     * @param buffer buffer to write the message parts to.
     */
    public void writeToMessage(byte[] buffer)
    {
        writeToMessage(buffer, 0);
    }

    /**
     * Returns true if a complete message has been written with {@link #writeToMessage(byte[])}.
     * @return true if a complete message has been written
     */
    public native boolean messageCompleted();

    /**
     * Processes an incoming data packet from the source. The internal
     * state of the sink might change: it can decode some original messages
     * and it might schedule additional snack packets.
     * @param buffer the buffer containing the data packet
     */
    public void readDataPacket(byte[] buffer) throws InvalidDataPacketException
    {
        readDataPacket(buffer, 0, buffer.length);
    }

    /**
     * Processes an incoming data packet from the source. The internal
     * state of the sink might change: it can decode some original messages
     * and it might schedule additional snack packets.
     * @param buffer the buffer containing the data packet
     * @param offset the offset into the buffer
     * @param size the size of the buffer
     */
    public native void readDataPacket(byte[] buffer, int offset, int size)  throws InvalidDataPacketException;

    /**
     * Returns if the sink has any snack packets at the moment.
     * @return true if the sink has any snack packets
     */
    public native boolean hasSnackPacket();

    /**
     * Returns a snack packet from the sink that should be transmitted to the source.
     * @return the snack packet
     * @throws IllegalStateException If no snack packet is available.
     * Use {@link Sink#hasSnackPacket()} to check.
     */
    public byte[] getSnackPacket()
    {
        if (!hasSnackPacket())
            throw new IllegalStateException("No snack packet available.");

        return nativeGetSnackPacket();
    }
    private native byte[] nativeGetSnackPacket();

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
