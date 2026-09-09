#pragma once

#include "tribetalk/pipeline/pipeline_types.h"
#include "tribetalk/resource/resource_manager.h"
#include <string>
#include <functional>
#include <memory>
#include <mutex>

namespace tribetalk {
namespace pipeline {

using StateChangeListener = std::function<void(PipelineState old_state, PipelineState new_state)>;

/**
 * Production-grade native C++ pipeline coordinating ASR -> NMT -> TTS
 * without requiring an embedded Python interpreter on Android.
 */
class TribeTalkPipeline {
public:
    TribeTalkPipeline();
    explicit TribeTalkPipeline(resource::MemoryMode memory_mode);
    ~TribeTalkPipeline();

    bool init(const std::string& asset_directory);
    void release_all();
    void trim_memory();

    PipelineResult process_speech(
        const audio::AudioBuffer& audio,
        PipelineDirection direction,
        bool synthesize_speech = true
    );

    PipelineResult process_text(
        const std::string& text,
        PipelineDirection direction,
        bool synthesize_speech = true
    );

    PipelineState get_state() const noexcept;
    void set_state_listener(StateChangeListener listener);

private:
    void set_state(PipelineState new_state);

    std::unique_ptr<resource::ResourceManager> resource_manager_;
    PipelineState state_;
    StateChangeListener state_listener_;
    mutable std::mutex mutex_;
};

} // namespace pipeline
} // namespace tribetalk
