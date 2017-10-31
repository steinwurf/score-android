package com.steinwurf.score.sender;

@SuppressWarnings("JniMissingFunction")
public class Version
{
    static
    {
        System.loadLibrary("score_sender_version_jni");
    }

    /**
     * Get a string specifying the version of the underlying libraries
     * @return string specifying the version of the underlying libraries
     */
    public static native String get();
}
