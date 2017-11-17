package com.steinwurf.score.sink;
/*-
 * Copyright (c) 2017 Steinwurf ApS
 * All Rights Reserved
 *
 * THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF STEINWURF
 * The copyright notice above does not evidence any
 * actual or intended publication of such source code.
 */

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