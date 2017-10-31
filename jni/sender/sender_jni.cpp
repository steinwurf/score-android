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

#include <score/api/manual_sender.hpp>

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

jlong Java_com_steinwurf_score_sender_Sender_init(
    JNIEnv* /*env*/, jclass /*thiz*/)
{
    return reinterpret_cast<jlong>(new score::api::manual_sender());
}
void Java_com_steinwurf_score_sender_Sender_writeData(
    JNIEnv* env, jobject thiz, jbyteArray jdata)
{
    auto data = jutils::java_byte_array_to_vector(env, jdata);
    auto& sender = jutils::get_native<score::api::manual_sender>(env, thiz);
    sender.write_data(data);
}

void Java_com_steinwurf_score_sender_Sender_flush(
    JNIEnv* env, jobject thiz)
{
    auto& sender = jutils::get_native<score::api::manual_sender>(env, thiz);
    sender.flush();
}

jboolean Java_com_steinwurf_score_sender_Sender_hasOutgoingMessage(
    JNIEnv* env, jobject thiz)
{
    auto& sender = jutils::get_native<score::api::manual_sender>(env, thiz);
    return sender.has_outgoing_message();
}

jint Java_com_steinwurf_score_sender_Sender_outgoingMessages(
    JNIEnv* env, jobject thiz)
{
    auto& sender = jutils::get_native<score::api::manual_sender>(env, thiz);
    return sender.outgoing_messages();
}

jbyteArray Java_com_steinwurf_score_sender_Sender_getOutgoingMessage(
    JNIEnv* env, jobject thiz)
{
    auto& sender = jutils::get_native<score::api::manual_sender>(env, thiz);

    auto outgoing_message = sender.get_outgoing_message();
    jbyteArray jmessage = env->NewByteArray(outgoing_message.size());
    env->SetByteArrayRegion(
        jmessage, 0,
        outgoing_message.size(),
        (const jbyte*)outgoing_message.data());
    return jmessage;
}

void Java_com_steinwurf_score_sender_Sender_receiveMessage(
    JNIEnv* env, jobject thiz, jbyteArray jbuffer)
{
    auto buffer = jutils::java_byte_array_to_vector(env, jbuffer);
    auto& sender = jutils::get_native<score::api::manual_sender>(env, thiz);
    std::error_code error;
    sender.receive_message(buffer, error);
    assert(!error);
}

void Java_com_steinwurf_score_sender_Sender_setSymbolSize(
    JNIEnv* env, jobject thiz, jint size)
{
    auto& sender = jutils::get_native<score::api::manual_sender>(env, thiz);
    sender.set_symbol_size(size);
}

void Java_com_steinwurf_score_sender_Sender_setGenerationSize(
    JNIEnv* env, jobject thiz, jint symbols)
{
    auto& sender = jutils::get_native<score::api::manual_sender>(env, thiz);
    sender.set_generation_size(symbols);
}

void Java_com_steinwurf_score_sender_Sender_setGenerationWindowSize(
    JNIEnv* env, jobject thiz, jint generations)
{
    auto& sender = jutils::get_native<score::api::manual_sender>(env, thiz);
    sender.set_generation_window_size(generations);
}

void Java_com_steinwurf_score_sender_Sender_setDataRedundancy(
    JNIEnv* env, jobject thiz, jfloat redundancy)
{
    auto& sender = jutils::get_native<score::api::manual_sender>(env, thiz);
    sender.set_data_redundancy(redundancy);
}

void Java_com_steinwurf_score_sender_Sender_setFeedbackProbability(
    JNIEnv* env, jobject thiz, jfloat probability)
{
    auto& sender = jutils::get_native<score::api::manual_sender>(env, thiz);
    sender.set_feedback_probability(probability);
}

jint Java_com_steinwurf_score_sender_Sender_generationWindowSize(
    JNIEnv* env, jobject thiz)
{
    auto& sender = jutils::get_native<score::api::manual_sender>(env, thiz);
    return sender.generation_window_size();
}

jfloat Java_com_steinwurf_score_sender_Sender_dataRedundancy(
    JNIEnv* env, jobject thiz)
{
    auto& sender = jutils::get_native<score::api::manual_sender>(env, thiz);
    return sender.data_redundancy();
}

jfloat Java_com_steinwurf_score_sender_Sender_feedbackProbability(
    JNIEnv* env, jobject thiz)
{
    auto& sender = jutils::get_native<score::api::manual_sender>(env, thiz);
    return sender.feedback_probability();
}

jint Java_com_steinwurf_score_sender_Sender_symbolSize(
    JNIEnv* env, jobject thiz)
{
    auto& sender = jutils::get_native<score::api::manual_sender>(env, thiz);
    return sender.symbol_size();
}

jint Java_com_steinwurf_score_sender_Sender_generationSize(
    JNIEnv* env, jobject thiz)
{
    auto& sender = jutils::get_native<score::api::manual_sender>(env, thiz);
    return sender.generation_size();
}


void Java_com_steinwurf_score_sender_Sender_finalize(
    JNIEnv* /*env*/, jobject /*thiz*/, jlong pointer)
{
    auto sender = reinterpret_cast<score::api::manual_sender*>(pointer);
    assert(sender);
    delete sender;
}

#ifdef __cplusplus
}
#endif
