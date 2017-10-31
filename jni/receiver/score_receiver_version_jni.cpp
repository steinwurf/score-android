// Copyright (c) 2016 Steinwurf ApS
// All Rights Reserved
//
// THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF STEINWURF
// The copyright notice above does not evidence any
// actual or intended publication of such source code.

#include <sstream>

#include <jni.h>

// To allow for overloading of functions, C++ uses something called name
// mangling.
// This means that function names are not the same in C++ as in plain C.
// To inhibit this name mangling, you have to declare functions as extern "C"
#ifdef __cplusplus
extern "C" {
#endif

jstring Java_com_steinwurf_score_receiver_Version_get(
    JNIEnv* env, jobject /*thiz*/)
{
    std::ostringstream version;

    version << "score-android: ";
#ifdef STEINWURF_SCORE_ANDROID_VERSION
    version << STEINWURF_SCORE_ANDROID_VERSION;
#else
    version << "-";
#endif

    version << "score: ";
#ifdef STEINWURF_SCORE_VERSION
    version << STEINWURF_SCORE_VERSION;
#else
    version << "-";
#endif

    version << "boost: ";
#ifdef STEINWURF_BOOST_VERSION
    version << STEINWURF_BOOST_VERSION;
#else
    version << "-";
#endif

    version << "links: ";
#ifdef STEINWURF_LINKS_VERSION
    version << STEINWURF_LINKS_VERSION;
#else
    version << "-";
#endif

    version << "endian: ";
#ifdef STEINWURF_ENDIAN_VERSION
    version << STEINWURF_ENDIAN_VERSION;
#else
    version << "-";
#endif

    version << "kodo-core: ";
#ifdef STEINWURF_KODO_CORE_VERSION
    version << STEINWURF_KODO_CORE_VERSION;
#else
    version << "-";
#endif

    version << "allocate: ";
#ifdef STEINWURF_ALLOCATE_VERSION
    version << STEINWURF_ALLOCATE_VERSION;
#else
    version << "-";
#endif

    version << "fifi: ";
#ifdef STEINWURF_FIFI_VERSION
    version << STEINWURF_FIFI_VERSION;
#else
    version << "-";
#endif

    version << "cpuid: ";
#ifdef STEINWURF_CPUID_VERSION
    version << STEINWURF_CPUID_VERSION;
#else
    version << "-";
#endif

    version << "platform: ";
#ifdef STEINWURF_PLATFORM_VERSION
    version << STEINWURF_PLATFORM_VERSION;
#else
    version << "-";
#endif

    version << "storage: ";
#ifdef STEINWURF_STORAGE_VERSION
    version << STEINWURF_STORAGE_VERSION;
#else
    version << "-";
#endif

    version << "recycle: ";
#ifdef STEINWURF_RECYCLE_VERSION
    version << STEINWURF_RECYCLE_VERSION;
#else
    version << "-";
#endif

    version << "meta: ";
#ifdef STEINWURF_META_VERSION
    version << STEINWURF_META_VERSION;
#else
    version << "-";
#endif

    version << "hex: ";
#ifdef STEINWURF_HEX_VERSION
    version << STEINWURF_HEX_VERSION;
#else
    version << "-";
#endif

    version << "chunkie: ";
#ifdef STEINWURF_CHUNKIE_VERSION
    version << STEINWURF_CHUNKIE_VERSION;
#else
    version << "-";
#endif

    version << "bitter: ";
#ifdef STEINWURF_BITTER_VERSION
    version << STEINWURF_BITTER_VERSION;
#else
    version << "-";
#endif

    return env->NewStringUTF(version.str().c_str());
}

#ifdef __cplusplus
}
#endif
