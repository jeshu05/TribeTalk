#pragma once

#include "tribetalk/audio/audio_buffer.h"
#include <string>
#include <vector>
#include <unordered_map>
#include <memory>
#include <mutex>

namespace tribetalk {
namespace tts {

struct TtsResult {
    audio::AudioBuffer audio;
    std::string text;
    std::string language;
    float processing_time_ms = 0.0f;
    float duration_ms = 0.0f;
};

/**
 * Production-grade C++ TTS Engine for VITS ONNX (Meta MMS Hindi & Piper Santali).
 */
class TtsEngine {
public:
    explicit TtsEngine(std::string language);
    ~TtsEngine();

    bool load(const std::string& model_path, const std::string& vocab_path);
    void unload();
    bool is_loaded() const noexcept;

    TtsResult synthesize(const std::string& text);

    const std::string& language() const noexcept { return language_; }

    // Helpers
    std::vector<int64_t> tokenize_with_blanks(const std::string& text) const;

private:
    bool load_vocab(const std::string& vocab_path);

    std::string language_;
    std::string model_path_;
    std::string vocab_path_;
    std::unordered_map<std::string, int64_t> vocab_;
    int64_t unk_token_ = 0;
    bool is_loaded_ = false;
    mutable std::mutex mutex_;
};

} // namespace tts
} // namespace tribetalk
