#pragma once

#include <cstdint>
#include <vector>
#include <cmath>
#include <algorithm>
#include <stdexcept>

namespace tribetalk {
namespace audio {

/**
 * Encapsulated audio PCM container holding 16 kHz 1D float32 audio samples.
 */
class AudioBuffer {
public:
    AudioBuffer() = default;
    explicit AudioBuffer(std::vector<float> samples, int32_t sample_rate = 16000);
    AudioBuffer(const float* data, size_t size, int32_t sample_rate = 16000);

    // Accessors
    const std::vector<float>& samples() const noexcept { return samples_; }
    std::vector<float>& samples() noexcept { return samples_; }
    int32_t sample_rate() const noexcept { return sample_rate_; }
    size_t size() const noexcept { return samples_.size(); }
    bool empty() const noexcept { return samples_.empty(); }

    // Metrics
    float duration_seconds() const noexcept;
    float duration_ms() const noexcept;
    float calculate_rms() const noexcept;
    float calculate_peak() const noexcept;

    // Mutators
    void normalize(float target_peak = 0.95f);
    void pad_silence(size_t num_samples);
    void clear() noexcept;
    AudioBuffer slice(size_t start_sample, size_t num_samples) const;

private:
    std::vector<float> samples_;
    int32_t sample_rate_ = 16000;
};

} // namespace audio
} // namespace tribetalk
