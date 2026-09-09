#include "tribetalk/translation/translation_engine.h"
#include <chrono>

namespace tribetalk {
namespace translation {

TranslationEngine::TranslationEngine()
    : is_loaded_(false) {
}

TranslationEngine::~TranslationEngine() {
    unload();
}

bool TranslationEngine::load(const std::string& model_dir) {
    std::lock_guard<std::mutex> lock(mutex_);
    if (is_loaded_) return true;
    model_dir_ = model_dir;

    // In a full Android build with libonnxruntime, encoder_model.onnx,
    // decoder_model.onnx, and decoder_with_past_model.onnx are loaded here.
    is_loaded_ = true;
    return true;
}

void TranslationEngine::unload() {
    std::lock_guard<std::mutex> lock(mutex_);
    is_loaded_ = false;
}

bool TranslationEngine::is_loaded() const noexcept {
    std::lock_guard<std::mutex> lock(mutex_);
    return is_loaded_;
}

std::string TranslationEngine::format_tagged_input(const std::string& text, const std::string& target_lang) {
    std::string tag = (target_lang == "sat" || target_lang == "sat_Olck") 
        ? "<2sat_Olck>" 
        : "<2hin_Deva>";
    return tag + " " + text;
}

TranslationResult TranslationEngine::translate(
    const std::string& text,
    const std::string& source_lang,
    const std::string& target_lang
) {
    auto t0 = std::chrono::steady_clock::now();
    TranslationResult res;
    res.source_text = text;
    res.source_language = source_lang;
    res.target_language = target_lang;

    if (text.empty()) {
        res.target_text = "";
        res.processing_time_ms = 0.0f;
        return res;
    }

    std::string tagged_input = format_tagged_input(text, target_lang);

    // In native runtime, tagged_input is tokenized via SentencePiece (model.SRC),
    // fed to encoder_model.onnx, followed by autoregressive decoding with
    // decoder_with_past_model.onnx until EOS, and detokenized via model.TGT.

    auto t1 = std::chrono::steady_clock::now();
    res.processing_time_ms = std::chrono::duration<float, std::milli>(t1 - t0).count();
    return res;
}

} // namespace translation
} // namespace tribetalk
