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
 * AutoSource wraps a native source object and gives access to its data members.
 */
@SuppressWarnings("JniMissingFunction")
public class AutoSource extends Source
{
    static
    {
        // Load the native source library
        System.loadLibrary("auto_source_jni");
    }

    /**
     * A long representing a pointer to the underlying native object.
     */
    private final long pointer;

    /**
     * Construct AutoSource object.
     */
    public AutoSource()
    {
        pointer = init();
    }

    /**
     * Construct a native Score source and returns a long value which represents
     * the pointer of the created object.
     */
    private static native long init();

    @Override
    public native void readMessage(byte[] buffer, int offset, int size);

    @Override
    public native void flush();

    @Override
    public native boolean hasDataPacket();

    @Override
    public native int dataPackets();

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
     * Set the maximum data redundancy that can be used
     * @param data_redundancy new value for max redundancy, must be greater than or equal to 0
     */
    public void setMaxDataRedundancy(float data_redundancy)
    {
        if (data_redundancy < 0)
            throw new IllegalArgumentException(data_redundancy + " < 0");
        nativeSetMaxDataRedundancy(data_redundancy);
    }
    private native void nativeSetMaxDataRedundancy(float data_redundancy);

    /**
     * The maximum data redundancy that can be used
     * @return the maximum data redundancy that can be used
     */
    public native float maxDataRedundancy();

    /**
     * Set the gain used to automatically adjust the data redundancy, a
     * higher value will track changes faster but also result in a less
     * steady redundancy level
     * @param gain float in the range ]0,1].
     */
    public void setDataRedundancyEstimationGain(float gain)
    {
        if (gain < 0)
            throw new IllegalArgumentException(gain + " < 0");
        nativeSetDataRedundancyEstimationGain(gain);
    }
    private native void nativeSetDataRedundancyEstimationGain(float gain);

    /**
     * The gain used to automatically adjust the data redundancy
     * @return the gain used to automatically adjust the data redundancy
     */
    public native float dataRedundancyEstimationGain();

    /**
     * Set the target number of snacks the source would like to read
     * per generation
     * @param snacks the number of target snacks per generation
     */
    public void setTargetSnacksPerGeneration(int snacks)
    {
        if (snacks < 0)
            throw new IllegalArgumentException(snacks + " < 0");
        nativeSetTargetSnacksPerGeneration(snacks);
    }
    private native void nativeSetTargetSnacksPerGeneration(int snacks);

    /**
     * The number of snacks the source would like to read per generation
     * @return Desired number of snacks per generation
     */
    public native int targetSnacksPerGeneration();

    /**
     * Set the minimum feedback probability that can be used
     * @param probability the min feedback probability
     */
    public void setMinFeedbackProbability(float probability)
    {
        if (probability < 0)
            throw new IllegalArgumentException(probability + " < 0");
        nativeSetMinFeedbackProbability(probability);
    }
    private native void nativeSetMinFeedbackProbability(float probability);

    /**
     * The minimum feedback probability that can be used
     * @return Minimum feedback probability
     */
    public native float minFeedbackProbability();

    /**
     * Set the maximum feedback probability that can be used
     * @param probability the max feedback probability
     */
    public void setMaxFeedbackProbability(float probability)
    {
        if (probability < 0)
            throw new IllegalArgumentException(probability + " < 0");
        nativeSetMaxFeedbackProbability(probability);
    }
    private native void nativeSetMaxFeedbackProbability(float probability);

    /**
     * The maximum feedback probability that can be used
     * @return Maximum feedback probability
     */
    public native float maxFeedbackProbability();

    /**
     * Set the gain used to automatically adjust the data redundancy, a
     * higher value will track changes faster but also result in a less
     * steady redundancy level
     * @param gain float in the range ]0,1], default 0.2
     */
    public void setFeedbackProbabilityGain(float gain)
    {
        if (gain < 0)
            throw new IllegalArgumentException(gain + " < 0");
        nativeSetFeedbackProbabilityGain(gain);
    }
    private native void nativeSetFeedbackProbabilityGain(float gain);

    /**
     * The gain used to automatically adjust the feedback probability
     * @return Feedback probability gain
     */
    public native float feedbackProbabilityGain();

    /**
     * Set the target repair delay in milliseconds
     * @param milliseconds_delay the target repair delay in milliseconds
     */
    public void setTargetRepairDelay(int milliseconds_delay)
    {
        if (milliseconds_delay < 0)
            throw new IllegalArgumentException(milliseconds_delay + " < 0");
        nativeSetTargetRepairDelay(milliseconds_delay);
    }
    private native void nativeSetTargetRepairDelay(int milliseconds_delay);

    /**
     * The target repair delay in milliseconds
     * @return Target repair delay in milliseconds
     */
    public native int targetRepairDelay();

    /**
     * Set the symbol size.
     * Has no effect on current active encoders.
     * @param size the symbols size in bytes
     */
    public void setSymbolSize(int size)
    {
        //if (size > MAX_SYMBOL_SIZE)
        //    throw new IllegalArgumentException(size + " > " + MAX_SYMBOL_SIZE);
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
        if (symbols > MAX_GENERATION_SIZE)
            throw new IllegalArgumentException(symbols + " > " + MAX_GENERATION_SIZE);
        nativeSetGenerationSize(symbols);
    }
    private native void nativeSetGenerationSize(int symbols);

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
