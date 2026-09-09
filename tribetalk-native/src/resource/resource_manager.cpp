#include "tribetalk/resource/resource_manager.h"

namespace tribetalk {
namespace resource {

ResourceManager::ResourceManager(MemoryMode mode)
    : mode_(mode) {
}

ResourceManager::~ResourceManager() {
    release_all();
}

void ResourceManager::set_asset_directory(std::string asset_dir) {
    std::lock_guard<std::mutex> lock(mutex_);
    asset_dir_ = std::move(asset_dir);
}

void ResourceManager::set_memory_mode(MemoryMode mode) {
    std::lock_guard<std::mutex> lock(mutex_);
    mode_ = mode;
}

void ResourceManager::evict_previous_if_sequential() {
    if (mode_ != MemoryMode::SEQUENTIAL) return;

    if (asr_hi_ && asr_hi_->is_loaded()) asr_hi_->unload();
    if (asr_sat_ && asr_sat_->is_loaded()) asr_sat_->unload();
    if (translation_ && translation_->is_loaded()) translation_->unload();
    if (tts_hi_ && tts_hi_->is_loaded()) tts_hi_->unload();
    if (tts_sat_ && tts_sat_->is_loaded()) tts_sat_->unload();
}

std::shared_ptr<asr::AsrEngine> ResourceManager::acquire_asr(const std::string& language) {
    std::lock_guard<std::mutex> lock(mutex_);
    bool is_hi = (language == "hi" || language == "hindi");

    if (is_hi) {
        if (!asr_hi_) asr_hi_ = std::make_shared<asr::AsrEngine>("hi");
        if (!asr_hi_->is_loaded()) {
            evict_previous_if_sequential();
            std::string model = asset_dir_ + "/asr/hindi/model.int8.onnx";
            std::string vocab = asset_dir_ + "/asr/hindi/vocab.txt";
            asr_hi_->load(model, vocab);
        }
        return asr_hi_;
    } else {
        if (!asr_sat_) asr_sat_ = std::make_shared<asr::AsrEngine>("sat");
        if (!asr_sat_->is_loaded()) {
            evict_previous_if_sequential();
            std::string model = asset_dir_ + "/asr/santali/model.int8.onnx";
            std::string vocab = asset_dir_ + "/asr/santali/vocab.txt";
            asr_sat_->load(model, vocab);
        }
        return asr_sat_;
    }
}

std::shared_ptr<translation::TranslationEngine> ResourceManager::acquire_translation() {
    std::lock_guard<std::mutex> lock(mutex_);
    if (!translation_) {
        translation_ = std::make_shared<translation::TranslationEngine>();
    }
    if (!translation_->is_loaded()) {
        evict_previous_if_sequential();
        translation_->load(asset_dir_ + "/translation");
    }
    return translation_;
}

std::shared_ptr<tts::TtsEngine> ResourceManager::acquire_tts(const std::string& language) {
    std::lock_guard<std::mutex> lock(mutex_);
    bool is_hi = (language == "hi" || language == "hindi");

    if (is_hi) {
        if (!tts_hi_) tts_hi_ = std::make_shared<tts::TtsEngine>("hi");
        if (!tts_hi_->is_loaded()) {
            evict_previous_if_sequential();
            std::string model = asset_dir_ + "/tts/hindi/model.onnx";
            std::string vocab = asset_dir_ + "/tts/hindi/vocab.json";
            tts_hi_->load(model, vocab);
        }
        return tts_hi_;
    } else {
        if (!tts_sat_) tts_sat_ = std::make_shared<tts::TtsEngine>("sat");
        if (!tts_sat_->is_loaded()) {
            evict_previous_if_sequential();
            std::string model = asset_dir_ + "/tts/santali/sat_piper_model.onnx";
            std::string vocab = asset_dir_ + "/tts/santali/sat_piper_model.onnx.json";
            tts_sat_->load(model, vocab);
        }
        return tts_sat_;
    }
}

void ResourceManager::release_all() {
    std::lock_guard<std::mutex> lock(mutex_);
    if (asr_hi_) asr_hi_->unload();
    if (asr_sat_) asr_sat_->unload();
    if (translation_) translation_->unload();
    if (tts_hi_) tts_hi_->unload();
    if (tts_sat_) tts_sat_->unload();
}

void ResourceManager::trim_memory() {
    release_all();
}

} // namespace resource
} // namespace tribetalk
