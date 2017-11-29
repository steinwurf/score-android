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

#include <score/api/auto_source.hpp>

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

jlong Java_com_steinwurf_score_source_AutoSource_init(
    JNIEnv* /*env*/, jclass /*thiz*/)
{
    return reinterpret_cast<jlong>(new score::api::auto_source());
}
void Java_com_steinwurf_score_source_AutoSource_readMessage(
    JNIEnv* env, jobject thiz, jbyteArray jmessage, jint offset, jint size)
{
    auto jmessage_ptr = env->GetByteArrayElements(jmessage, 0);
    auto jmessage_size = env->GetArrayLength(jmessage);
    assert(jmessage_size >= (offset + size));

    auto& source = jutils::get_native<score::api::auto_source>(env, thiz);
    source.read_message((uint8_t*)jmessage_ptr + offset, size);
    env->ReleaseByteArrayElements(jmessage, jmessage_ptr, JNI_ABORT);
}

void Java_com_steinwurf_score_source_AutoSource_flush(JNIEnv* env, jobject thiz)
{
    auto& source = jutils::get_native<score::api::auto_source>(env, thiz);
    source.flush();
}

jboolean Java_com_steinwurf_score_source_AutoSource_hasDataPacket(
    JNIEnv* env, jobject thiz)
{
    auto& source = jutils::get_native<score::api::auto_source>(env, thiz);
    return source.has_data_packet();
}

jint Java_com_steinwurf_score_source_AutoSource_dataPackets(
    JNIEnv* env, jobject thiz)
{
    auto& source = jutils::get_native<score::api::auto_source>(env, thiz);
    return source.data_packets();
}

jbyteArray Java_com_steinwurf_score_source_AutoSource_getDataPacket(
    JNIEnv* env, jobject thiz)
{
    auto& source = jutils::get_native<score::api::auto_source>(env, thiz);

    jbyteArray jdata_packet = env->NewByteArray(source.data_packet_size());
    jbyte* jdata_packet_ptr = env->GetByteArrayElements(jdata_packet, 0);
    source.write_data_packet((uint8_t*)jdata_packet_ptr);
    env->ReleaseByteArrayElements(jdata_packet, jdata_packet_ptr, 0);
    return jdata_packet;
}

void Java_com_steinwurf_score_source_AutoSource_readSnackPacket(
    JNIEnv* env, jobject thiz, jbyteArray jsnack_packet, jint offset, jint size)
{
    auto jsnack_packet_ptr = env->GetByteArrayElements(jsnack_packet, 0);
    auto jsnack_packet_size = env->GetArrayLength(jsnack_packet);
    assert(jsnack_packet_size >= (offset + size));

    auto& source = jutils::get_native<score::api::auto_source>(env, thiz);

    std::error_code error;
    source.read_snack_packet((uint8_t*)jsnack_packet_ptr + offset, size, error);
    env->ReleaseByteArrayElements(jsnack_packet, jsnack_packet_ptr, JNI_ABORT);
    if (error)
    {
        LOGI << "Error reading snack packet: " << error.message();

        auto exception_class = jutils::get_class(
            env, "com/steinwurf/score/source/InvalidSnackPacketException");
        env->ThrowNew(exception_class, error.message().c_str());
    }
}

jint Java_com_steinwurf_score_source_AutoSource_generationWindowSize(
    JNIEnv* env, jobject thiz)
{
    auto& source = jutils::get_native<score::api::auto_source>(env, thiz);
    return source.generation_window_size();
}

jfloat Java_com_steinwurf_score_source_AutoSource_dataRedundancy(
    JNIEnv* env, jobject thiz)
{
    auto& source = jutils::get_native<score::api::auto_source>(env, thiz);
    return source.data_redundancy();
}

jfloat Java_com_steinwurf_score_source_AutoSource_feedbackProbability(
    JNIEnv* env, jobject thiz)
{
    auto& source = jutils::get_native<score::api::auto_source>(env, thiz);
    return source.feedback_probability();
}

jint Java_com_steinwurf_score_source_AutoSource_symbolSize(
    JNIEnv* env, jobject thiz)
{
    auto& source = jutils::get_native<score::api::auto_source>(env, thiz);
    return source.symbol_size();
}

jint Java_com_steinwurf_score_source_AutoSource_generationSize(
    JNIEnv* env, jobject thiz)
{
    auto& source = jutils::get_native<score::api::auto_source>(env, thiz);
    return source.generation_size();
}

void Java_com_steinwurf_score_source_AutoSource_setSymbolSize(
    JNIEnv* env, jobject thiz, jint size)
{
    auto& source = jutils::get_native<score::api::auto_source>(env, thiz);
    source.set_symbol_size(size);
}

void Java_com_steinwurf_score_source_AutoSource_setGenerationSize(
    JNIEnv* env, jobject thiz, jint symbols)
{
    auto& source = jutils::get_native<score::api::auto_source>(env, thiz);
    source.set_generation_size(symbols);
}

void Java_com_steinwurf_score_source_AutoSource_setMaxDataRedundancy(
    JNIEnv* env, jobject thiz, jfloat data_redundancy)
{
    auto& source = jutils::get_native<score::api::auto_source>(env, thiz);
    source.set_max_data_redundancy(data_redundancy);
}

jfloat Java_com_steinwurf_score_source_AutoSource_maxDataRedundancy(
    JNIEnv* env, jobject thiz)
{
    auto& source = jutils::get_native<score::api::auto_source>(env, thiz);
    return source.max_data_redundancy();
}

void Java_com_steinwurf_score_source_AutoSource_setDataRedundancyEstimationGain(
    JNIEnv* env, jobject thiz, jfloat gain)
{
    auto& source = jutils::get_native<score::api::auto_source>(env, thiz);
    source.set_data_redundancy_estimation_gain(gain);
}

jfloat Java_com_steinwurf_score_source_AutoSource_dataRedundancyEstimationGain(
    JNIEnv* env, jobject thiz)
{
    auto& source = jutils::get_native<score::api::auto_source>(env, thiz);
    return source.data_redundancy_estimation_gain();
}

void Java_com_steinwurf_score_source_AutoSource_setTargetSnacksPerGeneration(
    JNIEnv* env, jobject thiz, jint snacks)
{
    auto& source = jutils::get_native<score::api::auto_source>(env, thiz);
    source.set_target_snacks_per_generation(snacks);
}

jint Java_com_steinwurf_score_source_AutoSource_targetSnacksPerGeneration(
    JNIEnv* env, jobject thiz)
{
    auto& source = jutils::get_native<score::api::auto_source>(env, thiz);
    return source.target_snacks_per_generation();
}

void Java_com_steinwurf_score_source_AutoSource_setMinFeedbackProbability(
    JNIEnv* env, jobject thiz, jfloat probability)
{
    auto& source = jutils::get_native<score::api::auto_source>(env, thiz);
    source.set_min_feedback_probability(probability);
}

jfloat Java_com_steinwurf_score_source_AutoSource_minFeedbackProbability(
    JNIEnv* env, jobject thiz)
{
    auto& source = jutils::get_native<score::api::auto_source>(env, thiz);
    return source.min_feedback_probability();
}

void Java_com_steinwurf_score_source_AutoSource_setMaxFeedbackProbability(
    JNIEnv* env, jobject thiz, jfloat probability)
{
    auto& source = jutils::get_native<score::api::auto_source>(env, thiz);
    source.set_max_feedback_probability(probability);
}

jfloat Java_com_steinwurf_score_source_AutoSource_maxFeedbackProbability(
    JNIEnv* env, jobject thiz)
{
    auto& source = jutils::get_native<score::api::auto_source>(env, thiz);
    return source.max_feedback_probability();
}

void Java_com_steinwurf_score_source_AutoSource_setFeedbackProbabilityGain(
    JNIEnv* env, jobject thiz, jfloat gain)
{
    auto& source = jutils::get_native<score::api::auto_source>(env, thiz);
    source.set_feedback_probability_gain(gain);
}

jfloat Java_com_steinwurf_score_source_AutoSource_feedbackProbabilityGain(
    JNIEnv* env, jobject thiz)
{
    auto& source = jutils::get_native<score::api::auto_source>(env, thiz);
    return source.feedback_probability_gain();
}

void Java_com_steinwurf_score_source_AutoSource_setTargetRepairDelay(
    JNIEnv* env, jobject thiz, jint milliseconds_delay)
{
    auto& source = jutils::get_native<score::api::auto_source>(env, thiz);
    source.set_target_repair_delay(milliseconds_delay);
}

jint Java_com_steinwurf_score_source_AutoSource_targetRepairDelay(
    JNIEnv* env, jobject thiz)
{
    auto& source = jutils::get_native<score::api::auto_source>(env, thiz);
    return source.target_repair_delay();
}

void Java_com_steinwurf_score_source_AutoSource_finalize(
    JNIEnv* /*env*/, jobject /*thiz*/, jlong pointer)
{
    auto source = reinterpret_cast<score::api::auto_source*>(pointer);
    assert(source);
    delete source;
}

#ifdef __cplusplus
}
#endif