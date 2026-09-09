#pragma once

#include "tribetalk/audio/audio_buffer.h"
#include <vector>
#include <cstdint>

namespace tribetalk {
namespace audio {

struct ConformerFeatureConfig {
    int32_t sample_rate = 16000;
    int32_t n_mels = 80;
    int32_t n_fft = 512;
    int32_t win_length = 400; // 25 ms
    int32_t hop_length = 160; // 10 ms
    int32_t pad_to = 16;
    float log_zero_guard = 5.960464477539063e-08f;
};

/**
 * 80-channel Log-Mel Filterbank extractor for IndicConformer.
 * Produces normalized 2D feature matrix [80, time_frames] matching NeMo/Sherpa-ONNX.
 */
class AudioFeatureExtractor {
public:
    explicit AudioFeatureExtractor(ConformerFeatureConfig config = ConformerFeatureConfig());

    /**
     * Compute normalized 80-channel log-mel features.
     * 
     * @param buffer Input 16 kHz audio buffer.
     * @param[out] out_features Output 1D flattened array of shape [n_mels * padded_time_frames].
     * @param[out] out_frames Number of padded time frames.
     */
    void extract_features(
        const AudioBuffer& buffer,
        std::vector<float>& out_features,
        int64_t& out_frames
    ) const;

private:
    void init_mel_filterbanks();
    void init_hann_window();
    float hz_to_mel(float hz) const;
    float mel_to_hz(float mel) const;

    ConformerFeatureConfig config_;
    std::vector<float> window_;
    std::vector<std::vector<float>> mel_filters_; // [n_mels, (n_fft / 2) + 1]
};

} // namespace audio
} // namespace tribetalk
