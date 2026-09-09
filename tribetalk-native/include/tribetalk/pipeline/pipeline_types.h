#pragma once

#include "tribetalk/audio/audio_buffer.h"
#include "tribetalk/asr/asr_engine.h"
#include "tribetalk/translation/translation_engine.h"
#include "tribetalk/tts/tts_engine.h"
#include <string>

namespace tribetalk {
namespace pipeline {

enum class PipelineState {
    IDLE,
    LISTENING,
    ASR_PROCESSING,
    TRANSLATING,
    SYNTHESIZING,
    PLAYBACK,
    COMPLETED,
    ERROR
};

enum class PipelineDirection {
    HINDI_TO_SANTALI = 0,
    SANTALI_TO_HINDI = 1
};

struct PipelineResult {
    PipelineDirection direction;
    std::string source_language;
    std::string target_language;
    std::string source_text;
    std::string target_text;
    asr::AsrResult asr;
    translation::TranslationResult nmt;
    tts::TtsResult tts;
    float total_latency_ms = 0.0f;
    bool success = true;
    std::string error_message;

    bool has_audio_output() const noexcept {
        return !tts.audio.empty();
    }
};

} // namespace pipeline
} // namespace tribetalk
