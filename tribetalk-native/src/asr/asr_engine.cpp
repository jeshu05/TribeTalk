#include "tribetalk/asr/asr_engine.h"
#include <fstream>
#include <sstream>
#include <chrono>
#include <algorithm>

namespace tribetalk {
namespace asr {

AsrEngine::AsrEngine(std::string language)
    : language_(std::move(language)), is_loaded_(false) {
}

AsrEngine::~AsrEngine() {
    unload();
}

bool AsrEngine::load_vocab(const std::string& vocab_path) {
    std::ifstream file(vocab_path);
    if (!file.is_open()) {
        return false;
    }

    vocab_.clear();
    std::string line;
    while (std::getline(file, line)) {
        if (line.empty()) continue;
        std::istringstream iss(line);
        std::string piece;
        int32_t idx;
        if (iss >> piece >> idx) {
            vocab_[idx] = piece;
            if (piece == "<blk>") {
                blank_id_ = idx;
            }
        }
    }
    return !vocab_.empty();
}

bool AsrEngine::load(const std::string& model_path, const std::string& vocab_path) {
    std::lock_guard<std::mutex> lock(mutex_);
    if (is_loaded_) return true;

    model_path_ = model_path;
    vocab_path_ = vocab_path;

    if (!load_vocab(vocab_path)) {
        return false;
    }

    // In a full Android build with libonnxruntime, Ort::Session is initialized here
    is_loaded_ = true;
    return true;
}

void AsrEngine::unload() {
    std::lock_guard<std::mutex> lock(mutex_);
    vocab_.clear();
    is_loaded_ = false;
}

bool AsrEngine::is_loaded() const noexcept {
    std::lock_guard<std::mutex> lock(mutex_);
    return is_loaded_;
}

std::string AsrEngine::decode_ctc(const std::vector<int32_t>& best_tokens) const {
    std::vector<int32_t> collapsed;
    int32_t prev = -1;

    for (int32_t tok : best_tokens) {
        if (tok != prev) {
            if (tok != blank_id_) {
                collapsed.push_back(tok);
            }
            prev = tok;
        }
    }

    std::string result;
    for (int32_t tok : collapsed) {
        auto it = vocab_.find(tok);
        if (it != vocab_.end()) {
            result += it->second;
        }
    }

    // Replace SentencePiece boundary symbol (\u2581 = 0xE2 0x96 0x81 in UTF-8) with space
    std::string clean;
    const std::string sp_boundary = "\xe2\x96\x81";
    size_t pos = 0;
    while (pos < result.size()) {
        if (result.compare(pos, sp_boundary.size(), sp_boundary) == 0) {
            clean += ' ';
            pos += sp_boundary.size();
        } else {
            clean += result[pos];
            pos++;
        }
    }

    // Trim leading and trailing whitespace
    size_t first = clean.find_first_not_of(" \t\n\r");
    if (first == std::string::npos) return "";
    size_t last = clean.find_last_not_of(" \t\n\r");
    return clean.substr(first, (last - first + 1));
}

AsrResult AsrEngine::transcribe(const audio::AudioBuffer& audio) {
    auto t0 = std::chrono::steady_clock::now();
    AsrResult res;
    res.language = language_;
    res.audio_duration_ms = audio.duration_ms();

    if (audio.empty()) {
        res.text = "";
        res.processing_time_ms = 0.0f;
        res.is_final = true;
        return res;
    }

    std::vector<float> features;
    int64_t num_frames = 0;
    feature_extractor_.extract_features(audio, features, num_frames);

    // In native runtime, features [1, 80, num_frames] are fed into ONNX session
    // yielding logprobs [1, num_frames, 257], followed by argmax and decode_ctc().

    auto t1 = std::chrono::steady_clock::now();
    res.processing_time_ms = std::chrono::duration<float, std::milli>(t1 - t0).count();
    res.is_final = true;
    return res;
}

} // namespace asr
} // namespace tribetalk
