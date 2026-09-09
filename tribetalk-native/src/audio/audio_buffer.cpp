#include "tribetalk/audio/audio_buffer.h"

namespace tribetalk {
namespace audio {

AudioBuffer::AudioBuffer(std::vector<float> samples, int32_t sample_rate)
    : samples_(std::move(samples)), sample_rate_(sample_rate) {
}

AudioBuffer::AudioBuffer(const float* data, size_t size, int32_t sample_rate)
    : samples_(data, data + size), sample_rate_(sample_rate) {
}

float AudioBuffer::duration_seconds() const noexcept {
    if (sample_rate_ <= 0) return 0.0f;
    return static_cast<float>(samples_.size()) / static_cast<float>(sample_rate_);
}

float AudioBuffer::duration_ms() const noexcept {
    return duration_seconds() * 1000.0f;
}

float AudioBuffer::calculate_rms() const noexcept {
    if (samples_.empty()) return 0.0f;
    double sum = 0.0;
    for (float s : samples_) {
        sum += static_cast<double>(s) * static_cast<double>(s);
    }
    return static_cast<float>(std::sqrt(sum / samples_.size()));
}

float AudioBuffer::calculate_peak() const noexcept {
    float peak = 0.0f;
    for (float s : samples_) {
        float abs_s = std::abs(s);
        if (abs_s > peak) peak = abs_s;
    }
    return peak;
}

void AudioBuffer::normalize(float target_peak) {
    float current_peak = calculate_peak();
    if (current_peak > 1e-6f && current_peak > target_peak) {
        float scale = target_peak / current_peak;
        for (float& s : samples_) {
            s *= scale;
        }
    }
}

void AudioBuffer::pad_silence(size_t num_samples) {
    samples_.resize(samples_.size() + num_samples, 0.0f);
}

void AudioBuffer::clear() noexcept {
    samples_.clear();
}

AudioBuffer AudioBuffer::slice(size_t start_sample, size_t num_samples) const {
    if (start_sample >= samples_.size()) {
        return AudioBuffer(std::vector<float>(), sample_rate_);
    }
    size_t actual_len = std::min(num_samples, samples_.size() - start_sample);
    std::vector<float> sub(samples_.begin() + start_sample, samples_.begin() + start_sample + actual_len);
    return AudioBuffer(std::move(sub), sample_rate_);
}

} // namespace audio
} // namespace tribetalk
