#pragma once

#include <string>
#include <vector>
#include <memory>
#include <mutex>

namespace tribetalk {
namespace translation {

struct TranslationResult {
    std::string source_text;
    std::string target_text;
    std::string source_language;
    std::string target_language;
    float processing_time_ms = 0.0f;
};

/**
 * Production-grade C++ Translation Engine for IndicTrans2 INT8 ONNX with past KV cache.
 */
class TranslationEngine {
public:
    TranslationEngine();
    ~TranslationEngine();

    bool load(const std::string& model_dir);
    void unload();
    bool is_loaded() const noexcept;

    TranslationResult translate(
        const std::string& text,
        const std::string& source_lang,
        const std::string& target_lang
    );

    // Helpers
    static std::string format_tagged_input(const std::string& text, const std::string& target_lang);

private:
    std::string model_dir_;
    bool is_loaded_ = false;
    mutable std::mutex mutex_;
};

} // namespace translation
} // namespace tribetalk
