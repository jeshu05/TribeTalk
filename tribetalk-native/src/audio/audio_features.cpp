#include "tribetalk/audio/audio_features.h"
#include <cmath>
#include <algorithm>
#include <numeric>

#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif

namespace tribetalk {
namespace audio {

AudioFeatureExtractor::AudioFeatureExtractor(ConformerFeatureConfig config)
    : config_(config) {
    init_hann_window();
    init_mel_filterbanks();
}

void AudioFeatureExtractor::init_hann_window() {
    window_.resize(config_.win_length);
    for (int32_t i = 0; i < config_.win_length; ++i) {
        window_[i] = 0.5f * (1.0f - std::cos(2.0f * static_cast<float>(M_PI) * i / static_cast<float>(config_.win_length)));
    }
}

float AudioFeatureExtractor::hz_to_mel(float hz) const {
    return 2595.0f * std::log10(1.0f + hz / 700.0f);
}

float AudioFeatureExtractor::mel_to_hz(float mel) const {
    return 700.0f * (std::pow(10.0f, mel / 2595.0f) - 1.0f);
}

void AudioFeatureExtractor::init_mel_filterbanks() {
    int32_t num_fft_bins = (config_.n_fft / 2) + 1;
    mel_filters_.assign(config_.n_mels, std::vector<float>(num_fft_bins, 0.0f));

    float low_freq = 0.0f;
    float high_freq = static_cast<float>(config_.sample_rate) / 2.0f;

    float low_mel = hz_to_mel(low_freq);
    float high_mel = hz_to_mel(high_freq);

    std::vector<float> mel_points(config_.n_mels + 2);
    for (int32_t i = 0; i < config_.n_mels + 2; ++i) {
        mel_points[i] = low_mel + (high_mel - low_mel) * static_cast<float>(i) / (config_.n_mels + 1);
    }

    std::vector<int32_t> bin_points(config_.n_mels + 2);
    for (int32_t i = 0; i < config_.n_mels + 2; ++i) {
        float hz = mel_to_hz(mel_points[i]);
        bin_points[i] = static_cast<int32_t>(std::floor((config_.n_fft + 1) * hz / config_.sample_rate));
    }

    for (int32_t m = 0; m < config_.n_mels; ++m) {
        int32_t left = bin_points[m];
        int32_t center = bin_points[m + 1];
        int32_t right = bin_points[m + 2];

        for (int32_t k = left; k < center && k < num_fft_bins; ++k) {
            if (center != left) {
                mel_filters_[m][k] = static_cast<float>(k - left) / static_cast<float>(center - left);
            }
        }
        for (int32_t k = center; k < right && k < num_fft_bins; ++k) {
            if (right != center) {
                mel_filters_[m][k] = static_cast<float>(right - k) / static_cast<float>(right - center);
            }
        }
    }
}

void AudioFeatureExtractor::extract_features(
    const AudioBuffer& buffer,
    std::vector<float>& out_features,
    int64_t& out_frames
) const {
    const std::vector<float>& samples = buffer.samples();
    int32_t num_fft_bins = (config_.n_fft / 2) + 1;

    // Pad audio to handle centering
    int32_t pad = config_.n_fft / 2;
    std::vector<float> padded_audio(samples.size() + 2 * pad, 0.0f);
    std::copy(samples.begin(), samples.end(), padded_audio.begin() + pad);

    // Number of time frames
    int32_t num_frames = 0;
    if (padded_audio.size() >= static_cast<size_t>(config_.win_length)) {
        num_frames = static_cast<int32_t>((padded_audio.size() - config_.win_length) / config_.hop_length) + 1;
    }
    if (num_frames <= 0) num_frames = 1;

    // [n_mels, num_frames]
    std::vector<std::vector<float>> mel_spec(config_.n_mels, std::vector<float>(num_frames, 0.0f));

    std::vector<float> frame_buffer(config_.n_fft, 0.0f);
    std::vector<float> power_spec(num_fft_bins, 0.0f);

    for (int32_t t = 0; t < num_frames; ++t) {
        size_t start = static_cast<size_t>(t * config_.hop_length);

        // Windowing
        std::fill(frame_buffer.begin(), frame_buffer.end(), 0.0f);
        for (int32_t i = 0; i < config_.win_length; ++i) {
            if (start + i < padded_audio.size()) {
                frame_buffer[i] = padded_audio[start + i] * window_[i];
            }
        }

        // Discrete Fourier Transform (magnitude squared)
        for (int32_t k = 0; k < num_fft_bins; ++k) {
            float real = 0.0f;
            float imag = 0.0f;
            for (int32_t n = 0; n < config_.n_fft; ++n) {
                float angle = -2.0f * static_cast<float>(M_PI) * k * n / config_.n_fft;
                real += frame_buffer[n] * std::cos(angle);
                imag += frame_buffer[n] * std::sin(angle);
            }
            power_spec[k] = (real * real + imag * imag);
        }

        // Apply mel filterbanks and log
        for (int32_t m = 0; m < config_.n_mels; ++m) {
            float energy = 0.0f;
            for (int32_t k = 0; k < num_fft_bins; ++k) {
                energy += power_spec[k] * mel_filters_[m][k];
            }
            mel_spec[m][t] = std::log(energy + config_.log_zero_guard);
        }
    }

    // Per-feature normalization along time frames: (x - mean) / (std + 1e-5)
    for (int32_t m = 0; m < config_.n_mels; ++m) {
        double sum = 0.0;
        for (int32_t t = 0; t < num_frames; ++t) {
            sum += mel_spec[m][t];
        }
        float mean = static_cast<float>(sum / num_frames);

        double sq_sum = 0.0;
        for (int32_t t = 0; t < num_frames; ++t) {
            float diff = mel_spec[m][t] - mean;
            sq_sum += diff * diff;
        }
        float std_dev = static_cast<float>(std::sqrt(sq_sum / num_frames));

        for (int32_t t = 0; t < num_frames; ++t) {
            mel_spec[m][t] = (mel_spec[m][t] - mean) / (std_dev + 1e-5f);
        }
    }

    // Pad time dimension to multiple of pad_to (16)
    int32_t pad_amount = (config_.pad_to - (num_frames % config_.pad_to)) % config_.pad_to;
    int32_t padded_frames = num_frames + pad_amount;
    out_frames = padded_frames;

    // Flatten to [n_mels, padded_frames]
    out_features.assign(config_.n_mels * padded_frames, 0.0f);
    for (int32_t m = 0; m < config_.n_mels; ++m) {
        for (int32_t t = 0; t < num_frames; ++t) {
            out_features[m * padded_frames + t] = mel_spec[m][t];
        }
    }
}

} // namespace audio
} // namespace tribetalk
