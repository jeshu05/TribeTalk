#include "tribetalk/pipeline/pipeline.h"
#include <chrono>

namespace tribetalk {
namespace pipeline {

TribeTalkPipeline::TribeTalkPipeline()
    : resource_manager_(std::make_unique<resource::ResourceManager>(resource::MemoryMode::SEQUENTIAL)),
      state_(PipelineState::IDLE) {
}

TribeTalkPipeline::TribeTalkPipeline(resource::MemoryMode memory_mode)
    : resource_manager_(std::make_unique<resource::ResourceManager>(memory_mode)),
      state_(PipelineState::IDLE) {
}

TribeTalkPipeline::~TribeTalkPipeline() {
    release_all();
}

bool TribeTalkPipeline::init(const std::string& asset_directory) {
    std::lock_guard<std::mutex> lock(mutex_);
    resource_manager_->set_asset_directory(asset_directory);
    set_state(PipelineState::IDLE);
    return true;
}

void TribeTalkPipeline::release_all() {
    std::lock_guard<std::mutex> lock(mutex_);
    if (resource_manager_) {
        resource_manager_->release_all();
    }
    set_state(PipelineState::IDLE);
}

void TribeTalkPipeline::trim_memory() {
    std::lock_guard<std::mutex> lock(mutex_);
    if (resource_manager_) {
        resource_manager_->trim_memory();
    }
    set_state(PipelineState::IDLE);
}

PipelineState TribeTalkPipeline::get_state() const noexcept {
    std::lock_guard<std::mutex> lock(mutex_);
    return state_;
}

void TribeTalkPipeline::set_state_listener(StateChangeListener listener) {
    std::lock_guard<std::mutex> lock(mutex_);
    state_listener_ = std::move(listener);
}

void TribeTalkPipeline::set_state(PipelineState new_state) {
    PipelineState old_state = state_;
    if (old_state == new_state) return;
    state_ = new_state;
    if (state_listener_) {
        state_listener_(old_state, new_state);
    }
}

PipelineResult TribeTalkPipeline::process_speech(
    const audio::AudioBuffer& audio,
    PipelineDirection direction,
    bool synthesize_speech
) {
    auto start_time = std::chrono::steady_clock::now();
    PipelineResult res;
    res.direction = direction;
    res.source_language = (direction == PipelineDirection::HINDI_TO_SANTALI) ? "hi" : "sat";
    res.target_language = (direction == PipelineDirection::HINDI_TO_SANTALI) ? "sat" : "hi";

    if (audio.empty()) {
        res.success = true;
        res.source_text = "";
        res.target_text = "";
        return res;
    }

    try {
        // 1. ASR Stage
        set_state(PipelineState::ASR_PROCESSING);
        auto asr_engine = resource_manager_->acquire_asr(res.source_language);
        res.asr = asr_engine->transcribe(audio);
        res.source_text = res.asr.text;

        if (res.source_text.empty()) {
            set_state(PipelineState::COMPLETED);
            res.success = true;
            auto end_time = std::chrono::steady_clock::now();
            res.total_latency_ms = std::chrono::duration<float, std::milli>(end_time - start_time).count();
            set_state(PipelineState::IDLE);
            return res;
        }

        // 2. Translation Stage
        set_state(PipelineState::TRANSLATING);
        auto nmt_engine = resource_manager_->acquire_translation();
        res.nmt = nmt_engine->translate(res.source_text, res.source_language, res.target_language);
        res.target_text = res.nmt.target_text;

        // 3. Synthesis Stage
        if (synthesize_speech && !res.target_text.empty()) {
            set_state(PipelineState::SYNTHESIZING);
            auto tts_engine = resource_manager_->acquire_tts(res.target_language);
            res.tts = tts_engine->synthesize(res.target_text);
        }

        set_state(PipelineState::COMPLETED);
        res.success = true;
    } catch (const std::exception& e) {
        set_state(PipelineState::ERROR);
        res.success = false;
        res.error_message = e.what();
    }

    auto end_time = std::chrono::steady_clock::now();
    res.total_latency_ms = std::chrono::duration<float, std::milli>(end_time - start_time).count();
    set_state(PipelineState::IDLE);
    return res;
}

PipelineResult TribeTalkPipeline::process_text(
    const std::string& text,
    PipelineDirection direction,
    bool synthesize_speech
) {
    auto start_time = std::chrono::steady_clock::now();
    PipelineResult res;
    res.direction = direction;
    res.source_language = (direction == PipelineDirection::HINDI_TO_SANTALI) ? "hi" : "sat";
    res.target_language = (direction == PipelineDirection::HINDI_TO_SANTALI) ? "sat" : "hi";
    res.source_text = text;

    if (text.empty()) {
        res.success = true;
        res.target_text = "";
        return res;
    }

    try {
        // 1. Translation Stage
        set_state(PipelineState::TRANSLATING);
        auto nmt_engine = resource_manager_->acquire_translation();
        res.nmt = nmt_engine->translate(text, res.source_language, res.target_language);
        res.target_text = res.nmt.target_text;

        // 2. Synthesis Stage
        if (synthesize_speech && !res.target_text.empty()) {
            set_state(PipelineState::SYNTHESIZING);
            auto tts_engine = resource_manager_->acquire_tts(res.target_language);
            res.tts = tts_engine->synthesize(res.target_text);
        }

        set_state(PipelineState::COMPLETED);
        res.success = true;
    } catch (const std::exception& e) {
        set_state(PipelineState::ERROR);
        res.success = false;
        res.error_message = e.what();
    }

    auto end_time = std::chrono::steady_clock::now();
    res.total_latency_ms = std::chrono::duration<float, std::milli>(end_time - start_time).count();
    set_state(PipelineState::IDLE);
    return res;
}

} // namespace pipeline
} // namespace tribetalk
