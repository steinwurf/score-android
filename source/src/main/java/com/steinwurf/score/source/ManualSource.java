package com.steinwurf.score.source;
/*-
 * Copyright (c) 2017 Steinwurf ApS
 * All Rights Reserved
 *
 * THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF STEINWURF
 * The copyright notice above does not evidence any
 * actual or intended publication of such source code.
 */


/**
 * <h1>Handle to a native Manual Score Source</h1>
 * ManualSource wraps a native source object and gives access to its data members.
 */
@SuppressWarnings("JniMissingFunction")
public class ManualSource extends Source
{
    static
    {
        // Load the native source library
        System.loadLibrary("manual_source_jni");
    }

    /**
     * A long representing a pointer to the underlying native object.
     */
    private final long pointer;

    public enum Profile {
        STREAM,
        OBJECT
    }

    /**
     * Construct ManualSource object.
     */
    public ManualSource()
    {
        this(Profile.STREAM);
    }

    /**
     * Construct ManualSource object.
     * @param profile The profile to use for this source
     */
    public ManualSource(Profile profile)
    {
        long ptr = 0;
        switch (profile)
        {
            case OBJECT:
                ptr = objectInit();
                break;
            case STREAM:
                ptr = streamInit();
                break;
        }
        pointer = ptr;
    }

    /**
     * Construct a native Score stream source and returns a long value which represents
     * the pointer of the created object.
     */
    private static native long streamInit();

    /**
     * Construct a native Score object source and returns a long value which represents
     * the pointer of the created object.
     */
    private static native long objectInit();

    @Override
    public native void readMessage(byte[] buffer, int offset, int size);

    @Override
    public native void flush();

    @Override
    public native boolean hasDataPacket();

    @Override
    public native byte[] nativeGetDataPacket();

    @Override
    public native void readSnackPacket(byte[] buffer, int offset, int size) throws InvalidSnackPacketException;

    @Override
    public native int generationWindowSize();

    @Override
    public native float dataRedundancy();

    @Override
    public native float feedbackProbability();

    @Override
    public native int symbolSize();

    @Override
    public native int generationSize();

    /**
     * Set the symbol size.
     * Has no effect on current active encoders.
     * @param size the symbols size in bytes
     */
    public void setSymbolSize(int size)
    {
        if (size < 0)
            throw new IllegalArgumentException(size + " < 0");
        nativeSetSymbolSize(size);
    }
    private native void nativeSetSymbolSize(int size);

    /**
     * Set the generation size.
     * Has no effect on current active encoders
     * @param symbols the number of symbols in the next created generation
     */
    public void setGenerationSize(int symbols)
    {
        if (symbols < 0)
            throw new IllegalArgumentException(symbols + " < 0");
        nativeSetGenerationSize(symbols);
    }
    private native void nativeSetGenerationSize(int symbols);

    /**
     * Set the amount of supported generations 'back in time'.
     * @param generations the number of old generations
     */
    public void setGenerationWindowSize(int generations)
    {
        if (generations < 0)
            throw new IllegalArgumentException(generations + " < 0");
        nativeSetGenerationWindowSize(generations);
    }
    private native void nativeSetGenerationWindowSize(int generations);

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
    public void setDataRedundancy(float redundancy)
    {
        if (redundancy < 0)
            throw new IllegalArgumentException(redundancy + " < 0");
        nativeSetDataRedundancy(redundancy);
    }
    private native void nativeSetDataRedundancy(float redundancy);

    /**
     * Set the feedback probability
     * @param probability the feedback probability between 0.0 and 1.0
     */
    public void setFeedbackProbability(float probability)
    {
        if (probability < 0)
            throw new IllegalArgumentException(probability + " < 0");
        nativeSetFeedbackProbability(probability);
    }
    private native void nativeSetFeedbackProbability(float probability);

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
