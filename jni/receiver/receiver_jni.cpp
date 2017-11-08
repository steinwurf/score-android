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
#include <score/api/receiver.hpp>

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

jlong Java_com_steinwurf_score_receiver_Receiver_init(
    JNIEnv* /*env*/, jclass /*thiz*/)
{
    return reinterpret_cast<jlong>(new score::api::receiver());
}

jboolean Java_com_steinwurf_score_receiver_Receiver_hasOutgoingMessage(
    JNIEnv* env, jobject thiz)
{
    auto& receiver = jutils::get_native<score::api::receiver>(env, thiz);
    return receiver.has_outgoing_message();
}

int Java_com_steinwurf_score_receiver_Receiver_outgoingMessages(
    JNIEnv* env, jobject thiz)
{
    auto& receiver = jutils::get_native<score::api::receiver>(env, thiz);
    return receiver.outgoing_messages();
}

jbyteArray Java_com_steinwurf_score_receiver_Receiver_getOutgoingMessage(
    JNIEnv* env, jobject thiz)
{
    auto& receiver = jutils::get_native<score::api::receiver>(env, thiz);

    auto outgoing_message = receiver.get_outgoing_message();
    jbyteArray jmessage = env->NewByteArray(outgoing_message.size());
    env->SetByteArrayRegion(
        jmessage, 0,
        outgoing_message.size(),
        (const jbyte*)outgoing_message.data());
    return jmessage;
}

void Java_com_steinwurf_score_receiver_Receiver_receiveMessage(
    JNIEnv* env, jobject thiz, jbyteArray jbuffer, jint offset, jint size)
{
    auto buffer = jutils::java_byte_array_to_vector(env, jbuffer, offset, size);
    auto& receiver = jutils::get_native<score::api::receiver>(env, thiz);
    std::error_code error;
    receiver.receive_message(buffer, error);
    if (error)
    {
        LOGI << "Error reading data: " << error.message();

        auto exception_class = jutils::get_class(
            env, "com/steinwurf/score/receiver/Receiver$InvalidDataMessageException");
        env->ThrowNew(exception_class, error.message().c_str());
    }
}

jboolean Java_com_steinwurf_score_receiver_Receiver_dataAvailable(
    JNIEnv* env, jobject thiz)
{
    auto& receiver = jutils::get_native<score::api::receiver>(env, thiz);
    return receiver.data_available();
}

jbyteArray Java_com_steinwurf_score_receiver_Receiver_getData(
    JNIEnv* env, jobject thiz)
{
    auto& receiver = jutils::get_native<score::api::receiver>(env, thiz);

    std::error_code error;
    auto data = receiver.get_data(error);
    if (error)
    {
        LOGI << "Checksum Error: " << error.message();

        auto exception_class = jutils::get_class(
            env, "com/steinwurf/score/receiver/Receiver$InvalidChecksumException");
        env->ThrowNew(exception_class, error.message().c_str());
        return nullptr;
    }

    jbyteArray jdata = env->NewByteArray(data.size());
    env->SetByteArrayRegion(jdata, 0, data.size(), (const jbyte*)data.data());
    return jdata;
}

void Java_com_steinwurf_score_receiver_Receiver_finalize(
    JNIEnv* /*env*/, jobject /*thiz*/, jlong pointer)
{
    auto receiver = reinterpret_cast<score::api::receiver*>(pointer);
    assert(receiver);
    delete receiver;
}

#ifdef __cplusplus
}
#endif
