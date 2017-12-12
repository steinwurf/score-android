// Copyright (c) 2016 Steinwurf ApS
// All Rights Reserved
//
// THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF STEINWURF
// The copyright notice above does not evidence any
// actual or intended publication of such source code.

#include <cassert>
#include <cstdint>
#include <functional>
#include <system_error>

#include <jni.h>

#include <jutils/utils.hpp>
#include <jutils/logging.hpp>
#include <score/api/sink.hpp>

jint JNI_OnLoad(JavaVM* vm, void* /*reserved*/)
{
    jutils::init(vm);
    return JNI_VERSION_1_4;
}

// To allow for overloading of functions, C++ uses something called name
// mangling.
// This means that function names are not the same in C++ as in plain C.
// To inhibit this name mangling, you have to declare functions as extern "C"
#ifdef __cplusplus
extern "C" {
#endif

jlong Java_com_steinwurf_score_sink_Sink_init(JNIEnv* /*env*/, jclass /*thiz*/)
{
    return reinterpret_cast<jlong>(new score::api::sink());
}

jboolean Java_com_steinwurf_score_sink_Sink_hasSnackPacket(
    JNIEnv* env, jobject thiz)
{
    auto& sink = jutils::get_native<score::api::sink>(env, thiz);
    return sink.has_snack_packet();
}

int Java_com_steinwurf_score_sink_Sink_snackPackets(JNIEnv* env, jobject thiz)
{
    auto& sink = jutils::get_native<score::api::sink>(env, thiz);
    return sink.snack_packets();
}

jbyteArray Java_com_steinwurf_score_sink_Sink_nativeGetSnackPacket(
    JNIEnv* env, jobject thiz)
{
    auto& sink = jutils::get_native<score::api::sink>(env, thiz);
    jbyteArray jsnack_packet = env->NewByteArray(sink.snack_packet_size());
    jbyte* jsnack_packet_ptr = env->GetByteArrayElements(jsnack_packet, 0);
    sink.write_snack_packet((uint8_t*)jsnack_packet_ptr);
    env->ReleaseByteArrayElements(jsnack_packet, jsnack_packet_ptr, 0);
    return jsnack_packet;
}

void Java_com_steinwurf_score_sink_Sink_readDataPacket(
    JNIEnv* env, jobject thiz, jbyteArray jdata_packet, jint offset, jint size)
{
    auto jdata_packet_ptr = env->GetByteArrayElements(jdata_packet, 0);
    auto jdata_packet_size = env->GetArrayLength(jdata_packet);
    assert(jdata_packet_size >= (offset + size));

    auto& sink = jutils::get_native<score::api::sink>(env, thiz);

    std::error_code error;
    sink.read_data_packet((uint8_t*)jdata_packet_ptr + offset, size, error);
    env->ReleaseByteArrayElements(jdata_packet, jdata_packet_ptr, JNI_ABORT);
    if (error)
    {
        LOGI << "Error reading data packet: " << error.message();

        auto exception_class = jutils::get_class(
            env, "com/steinwurf/score/sink/Sink$InvalidDataPacketException");
        env->ThrowNew(exception_class, error.message().c_str());
    }
}

jboolean Java_com_steinwurf_score_sink_Sink_hasMessage(
    JNIEnv* env, jobject thiz)
{
    auto& sink = jutils::get_native<score::api::sink>(env, thiz);
    return sink.has_message();
}

jbyteArray Java_com_steinwurf_score_sink_Sink_nativeGetMessage(
    JNIEnv* env, jobject thiz)
{
    auto& sink = jutils::get_native<score::api::sink>(env, thiz);

    jbyteArray jmessage = env->NewByteArray(sink.message_size());
    jbyte* jmessage_ptr = env->GetByteArrayElements(jmessage, 0);

    std::error_code error;
    sink.write_message((uint8_t*)jmessage_ptr, error);
    env->ReleaseByteArrayElements(jmessage, jmessage_ptr, 0);
    if (error)
    {
        LOGI << "Checksum Error: " << error.message();

        auto exception_class = jutils::get_class(
            env, "com/steinwurf/score/sink/Sink$InvalidChecksumException");
        env->ThrowNew(exception_class, error.message().c_str());
        return nullptr;
    }
    return jmessage;
}

void Java_com_steinwurf_score_sink_Sink_finalize(
    JNIEnv* /*env*/, jobject /*thiz*/, jlong pointer)
{
    auto sink = reinterpret_cast<score::api::sink*>(pointer);
    assert(sink);
    delete sink;
}

#ifdef __cplusplus
}
#endif
