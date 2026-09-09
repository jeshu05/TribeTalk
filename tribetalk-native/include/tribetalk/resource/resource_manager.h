#pragma once

#include "tribetalk/asr/asr_engine.h"
#include "tribetalk/translation/translation_engine.h"
#include "tribetalk/tts/tts_engine.h"
#include <string>
#include <memory>
#include <mutex>
#include <unordered_map>

namespace tribetalk {
namespace resource {

enum class MemoryMode {
    SEQUENTIAL, // Strictly one model in memory at any instant (low-end Android target <= 950 MB)
    CACHED      // Retains models up to pool limit
};

/**
 * Thread-safe global model resource manager for native C++ inference.
 */
class ResourceManager {
public:
    explicit ResourceManager(MemoryMode mode = MemoryMode::SEQUENTIAL);
    ~ResourceManager();

    void set_asset_directory(std::string asset_dir);
    void set_memory_mode(MemoryMode mode);

    std::shared_ptr<asr::AsrEngine> acquire_asr(const std::string& language);
    std::shared_ptr<translation::TranslationEngine> acquire_translation();
    std::shared_ptr<tts::TtsEngine> acquire_tts(const std::string& language);

    void release_all();
    void trim_memory();

private:
    void evict_previous_if_sequential();

    MemoryMode mode_;
    std::string asset_dir_;

    std::shared_ptr<asr::AsrEngine> asr_hi_;
    std::shared_ptr<asr::AsrEngine> asr_sat_;
    std::shared_ptr<translation::TranslationEngine> translation_;
    std::shared_ptr<tts::TtsEngine> tts_hi_;
    std::shared_ptr<tts::TtsEngine> tts_sat_;

    mutable std::mutex mutex_;
};

} // namespace resource
} // namespace tribetalk
