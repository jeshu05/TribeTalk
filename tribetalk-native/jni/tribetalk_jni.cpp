#include <jni.h>
#include "tribetalk/pipeline/pipeline.h"
#include "tribetalk/slm/qwen_engine.h"
#include <string>
#include <vector>

using namespace tribetalk::pipeline;
using namespace tribetalk::audio;
using namespace tribetalk::slm;

static TribeTalkPipeline* g_pipeline = nullptr;
static QwenEngine* g_qwen_engine = nullptr;

extern "C" {

JNIEXPORT jboolean JNICALL
Java_org_tribetalk_core_NativePipeline_nativeInit(
    JNIEnv* env,
    jobject /* thiz */,
    jstring j_asset_dir
) {
    const char* asset_dir = env->GetStringUTFChars(j_asset_dir, nullptr);
    if (!g_pipeline) {
        g_pipeline = new TribeTalkPipeline();
    }
    bool success = g_pipeline->init(asset_dir ? asset_dir : "");
    if (asset_dir) {
        env->ReleaseStringUTFChars(j_asset_dir, asset_dir);
    }
    return static_cast<jboolean>(success);
}

JNIEXPORT void JNICALL
Java_org_tribetalk_core_NativePipeline_nativeReleaseAll(
    JNIEnv* /* env */,
    jobject /* thiz */
) {
    if (g_pipeline) {
        g_pipeline->release_all();
    }
}

JNIEXPORT void JNICALL
Java_org_tribetalk_core_NativePipeline_nativeTrimMemory(
    JNIEnv* /* env */,
    jobject /* thiz */
) {
    if (g_pipeline) {
        g_pipeline->trim_memory();
    }
}

JNIEXPORT jint JNICALL
Java_org_tribetalk_core_NativePipeline_nativeGetState(
    JNIEnv* /* env */,
    jobject /* thiz */
) {
    if (!g_pipeline) return static_cast<jint>(PipelineState::IDLE);
    return static_cast<jint>(g_pipeline->get_state());
}

JNIEXPORT jstring JNICALL
Java_org_tribetalk_core_NativePipeline_nativeProcessText(
    JNIEnv* env,
    jobject /* thiz */,
    jstring j_text,
    jint j_direction,
    jboolean j_synthesize
) {
    if (!g_pipeline) {
        return env->NewStringUTF("");
    }

    const char* text_str = env->GetStringUTFChars(j_text, nullptr);
    PipelineDirection dir = (j_direction == 0) 
        ? PipelineDirection::HINDI_TO_SANTALI 
        : PipelineDirection::SANTALI_TO_HINDI;

    PipelineResult res = g_pipeline->process_text(
        text_str ? text_str : "",
        dir,
        static_cast<bool>(j_synthesize)
    );

    if (text_str) {
        env->ReleaseStringUTFChars(j_text, text_str);
    }

    return env->NewStringUTF(res.target_text.c_str());
}

JNIEXPORT jfloatArray JNICALL
Java_org_tribetalk_core_NativePipeline_nativeProcessSpeech(
    JNIEnv* env,
    jobject /* thiz */,
    jfloatArray j_audio,
    jint j_sample_rate,
    jint j_direction,
    jboolean j_synthesize
) {
    if (!g_pipeline) {
        return env->NewFloatArray(0);
    }

    jsize len = env->GetArrayLength(j_audio);
    jfloat* audio_data = env->GetFloatArrayElements(j_audio, nullptr);

    AudioBuffer buf(audio_data, static_cast<size_t>(len), j_sample_rate);
    env->ReleaseFloatArrayElements(j_audio, audio_data, JNI_ABORT);

    PipelineDirection dir = (j_direction == 0) 
        ? PipelineDirection::HINDI_TO_SANTALI 
        : PipelineDirection::SANTALI_TO_HINDI;

    PipelineResult res = g_pipeline->process_speech(
        buf,
        dir,
        static_cast<bool>(j_synthesize)
    );

    if (!res.has_audio_output()) {
        return env->NewFloatArray(0);
    }

    const std::vector<float>& out_samples = res.tts.audio.samples();
    jfloatArray j_result = env->NewFloatArray(static_cast<jsize>(out_samples.size()));
    env->SetFloatArrayRegion(j_result, 0, static_cast<jsize>(out_samples.size()), out_samples.data());
    return j_result;
}

JNIEXPORT jobject JNICALL
Java_org_tribetalk_core_NativePipeline_nativeProcessSpeechFull(
    JNIEnv* env,
    jobject /* thiz */,
    jfloatArray j_audio,
    jint j_sample_rate,
    jint j_direction,
    jboolean j_synthesize
) {
    jclass clazz = env->FindClass("org/tribetalk/core/TranslationExchange");
    if (!clazz) {
        return nullptr;
    }

    jmethodID ctor = env->GetMethodID(
        clazz,
        "<init>",
        "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[FFZLjava/lang/String;)V"
    );
    if (!ctor) {
        return nullptr;
    }

    if (!g_pipeline) {
        jstring empty = env->NewStringUTF("");
        jfloatArray empty_arr = env->NewFloatArray(0);
        return env->NewObject(clazz, ctor, empty, empty, empty, empty, empty_arr, 0.0f, false, env->NewStringUTF("Pipeline not initialized"));
    }

    jsize len = env->GetArrayLength(j_audio);
    jfloat* audio_data = env->GetFloatArrayElements(j_audio, nullptr);

    AudioBuffer buf(audio_data, static_cast<size_t>(len), j_sample_rate);
    env->ReleaseFloatArrayElements(j_audio, audio_data, JNI_ABORT);

    PipelineDirection dir = (j_direction == 0) 
        ? PipelineDirection::HINDI_TO_SANTALI 
        : PipelineDirection::SANTALI_TO_HINDI;

    PipelineResult res = g_pipeline->process_speech(
        buf,
        dir,
        static_cast<bool>(j_synthesize)
    );

    jstring j_src_text = env->NewStringUTF(res.source_text.c_str());
    jstring j_tgt_text = env->NewStringUTF(res.target_text.c_str());
    jstring j_src_lang = env->NewStringUTF(res.source_language.c_str());
    jstring j_tgt_lang = env->NewStringUTF(res.target_language.c_str());
    jstring j_err_msg = env->NewStringUTF(res.error_message.c_str());

    jfloatArray j_audio_out;
    if (res.has_audio_output()) {
        const auto& samples = res.tts.audio.samples();
        j_audio_out = env->NewFloatArray(static_cast<jsize>(samples.size()));
        env->SetFloatArrayRegion(j_audio_out, 0, static_cast<jsize>(samples.size()), samples.data());
    } else {
        j_audio_out = env->NewFloatArray(0);
    }

    jobject j_result = env->NewObject(
        clazz,
        ctor,
        j_src_text,
        j_tgt_text,
        j_src_lang,
        j_tgt_lang,
        j_audio_out,
        static_cast<jfloat>(res.total_latency_ms),
        static_cast<jboolean>(res.success),
        j_err_msg
    );

    return j_result;
}

JNIEXPORT jboolean JNICALL
Java_org_tribetalk_core_NativePipeline_nativeQwenInit(
    JNIEnv* env,
    jobject /* thiz */,
    jstring j_model_dir
) {
    const char* model_dir = env->GetStringUTFChars(j_model_dir, nullptr);
    if (!g_qwen_engine) {
        g_qwen_engine = new QwenEngine();
    }
    bool success = g_qwen_engine->initialize(model_dir ? model_dir : "");
    if (model_dir) {
        env->ReleaseStringUTFChars(j_model_dir, model_dir);
    }
    return static_cast<jboolean>(success);
}

JNIEXPORT jboolean JNICALL
Java_org_tribetalk_core_NativePipeline_nativeQwenLoad(
    JNIEnv* /* env */,
    jobject /* thiz */
) {
    if (!g_qwen_engine) {
        g_qwen_engine = new QwenEngine();
    }
    return static_cast<jboolean>(g_qwen_engine->load_model());
}

JNIEXPORT jstring JNICALL
Java_org_tribetalk_core_NativePipeline_nativeQwenGenerate(
    JNIEnv* env,
    jobject /* thiz */,
    jstring j_prompt,
    jint max_tokens,
    jfloat temperature,
    jint seed
) {
    if (!g_qwen_engine) {
        return env->NewStringUTF("{}");
    }
    const char* prompt = env->GetStringUTFChars(j_prompt, nullptr);
    QwenGenerationConfig config;
    config.max_tokens = max_tokens;
    config.temperature = temperature;
    config.seed = seed;

    QwenGenerationResult res = g_qwen_engine->generate(prompt ? prompt : "", config);

    if (prompt) {
        env->ReleaseStringUTFChars(j_prompt, prompt);
    }
    return env->NewStringUTF(res.text.c_str());
}

JNIEXPORT void JNICALL
Java_org_tribetalk_core_NativePipeline_nativeQwenCancel(
    JNIEnv* /* env */,
    jobject /* thiz */
) {
    if (g_qwen_engine) {
        g_qwen_engine->cancel_generation();
    }
}

JNIEXPORT void JNICALL
Java_org_tribetalk_core_NativePipeline_nativeQwenUnload(
    JNIEnv* /* env */,
    jobject /* thiz */
) {
    if (g_qwen_engine) {
        g_qwen_engine->unload_model();
    }
}

JNIEXPORT jstring JNICALL
Java_org_tribetalk_core_NativePipeline_nativeQwenGetModelInfo(
    JNIEnv* env,
    jobject /* thiz */
) {
    if (!g_qwen_engine) {
        return env->NewStringUTF("{\"model\":\"Qwen2.5-0.5B-Instruct\",\"is_loaded\":false}");
    }
    std::string info = g_qwen_engine->get_model_info();
    return env->NewStringUTF(info.c_str());
}

} // extern "C"

