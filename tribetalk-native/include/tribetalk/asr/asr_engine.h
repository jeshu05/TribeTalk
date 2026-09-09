#pragma once

#include "tribetalk/audio/audio_buffer.h"
#include "tribetalk/audio/audio_features.h"
#include <string>
#include <vector>
#include <unordered_map>
#include <memory>
#include <mutex>

namespace tribetalk {
namespace asr {

struct AsrResult {
    std::string text;
    std::string language;
    float processing_time_ms = 0.0f;
    float audio_duration_ms = 0.0f;
    bool is_final = true;
};

/**
 * Production-grade C++ ASR Engine for IndicConformer CTC (Hindi & Santali).
 */
class AsrEngine {
public:
    explicit AsrEngine(std::string language);
    ~AsrEngine();

    bool load(const std::string& model_path, const std::string& vocab_path);
    void unload();
    bool is_loaded() const noexcept;

    AsrResult transcribe(const audio::AudioBuffer& audio);

    const std::string& language() const noexcept { return language_; }

    // Helper for CTC decoding
    std::string decode_ctc(const std::vector<int32_t>& best_tokens) const;

private:
    bool load_vocab(const std::string& vocab_path);

    std::string language_;
    std::string model_path_;
    std::string vocab_path_;
    std::unordered_map<int32_t, std::string> vocab_;
    int32_t blank_id_ = 256;
    bool is_loaded_ = false;

    audio::AudioFeatureExtractor feature_extractor_;
    mutable std::mutex mutex_;
};

} // namespace asr
} // namespace tribetalk
