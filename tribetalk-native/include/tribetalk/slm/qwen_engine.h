#ifndef TRIBETALK_SLM_QWEN_ENGINE_H_
#define TRIBETALK_SLM_QWEN_ENGINE_H_

#include <string>
#include <vector>
#include <mutex>
#include <atomic>
#include <memory>

namespace tribetalk {
namespace slm {

enum class ModelLifecycleState {
    COLD = 0,
    LOADING = 1,
    WARM = 2,
    GENERATING = 3,
    IDLE = 4,
    UNLOADED = 5
};

struct QwenGenerationConfig {
    int max_tokens = 128;
    float temperature = 0.7f;
    float top_p = 0.9f;
    int seed = 42;
    std::string stop_token = "<|im_end|>";
};

struct QwenGenerationResult {
    std::string text;
    int prompt_tokens = 0;
    int generated_tokens = 0;
    float first_token_latency_ms = 0.0f;
    float total_latency_ms = 0.0f;
    float tokens_per_second = 0.0f;
    bool cancelled = false;
    bool success = false;
    std::string error_message;
};

class QwenEngine {
public:
    QwenEngine();
    ~QwenEngine();

    bool initialize(const std::string& model_dir);
    bool load_model();
    QwenGenerationResult generate(const std::string& prompt, const QwenGenerationConfig& config);
    void cancel_generation();
    void unload_model();
    bool is_loaded() const noexcept;
    ModelLifecycleState get_state() const noexcept;
    std::string get_model_info() const;

private:
    std::string model_dir_;
    std::string model_file_path_;
    std::string tokenizer_path_;
    mutable std::mutex mutex_;
    std::atomic<bool> is_loaded_{false};
    std::atomic<bool> cancel_requested_{false};
    std::atomic<ModelLifecycleState> state_{ModelLifecycleState::COLD};
};

} // namespace slm
} // namespace tribetalk

#endif // TRIBETALK_SLM_QWEN_ENGINE_H_
