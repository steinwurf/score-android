package com.steinwurf.score.sink;

@SuppressWarnings("JniMissingFunction")
public class Version
{
    static
    {
        System.loadLibrary("score_sink_version_jni");
    }

    /**
     * Get a string specifying the version of the underlying libraries
     * @return string specifying the version of the underlying libraries
     */
    public static native String get();
}