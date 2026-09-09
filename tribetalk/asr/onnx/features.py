"""Pure NumPy audio feature extractor for IndicConformer CTC models.

Computes 80-channel log-mel filterbank spectrograms with per-feature normalization
matching NeMo and Sherpa-ONNX specification without any PyTorch or NeMo dependency.
"""

from __future__ import annotations

import numpy as np
import librosa

LOG_ZERO_GUARD: float = 5.960464477539063e-08  # 2^-24


def extract_conformer_features(
    audio: np.ndarray,
    sample_rate: int = 16000,
    n_mels: int = 80,
    n_fft: int = 512,
    win_length: int = 400,
    hop_length: int = 160,
    pad_to: int = 16,
) -> np.ndarray:
    """Extract normalized 80-channel log-mel spectrogram features.

    Args:
        audio: 1D float32 audio waveform normalized to [-1.0, 1.0].
        sample_rate: Sampling rate in Hz (16,000 Hz).
        n_mels: Number of mel filterbank bins (80).
        n_fft: FFT window size (512).
        win_length: Window length in samples (400 = 25 ms).
        hop_length: Hop length in samples (160 = 10 ms).
        pad_to: Multiple to pad the time dimension (16).

    Returns:
        3D float32 array shaped [1, n_mels, padded_time_frames].
    """
    if audio.ndim != 1:
        audio = audio.flatten()

    if len(audio) == 0:
        audio = np.zeros(win_length, dtype=np.float32)

    # 1. Compute 80-channel mel spectrogram
    mel = librosa.feature.melspectrogram(
        y=audio,
        sr=sample_rate,
        n_fft=n_fft,
        win_length=win_length,
        hop_length=hop_length,
        n_mels=n_mels,
        window="hann",
        center=True,
        pad_mode="reflect",
    )

    # 2. Add log zero guard and take natural log
    log_mel = np.log(mel + LOG_ZERO_GUARD)

    # 3. Per-feature normalization along the time axis: (x - mean) / (std + 1e-5)
    mean = np.mean(log_mel, axis=1, keepdims=True)
    std = np.std(log_mel, axis=1, keepdims=True)
    norm_mel = (log_mel - mean) / (std + 1e-5)

    # 4. Pad time dimension to multiple of pad_to (16)
    time_len = norm_mel.shape[1]
    pad_amount = (pad_to - (time_len % pad_to)) % pad_to
    if pad_amount > 0:
        norm_mel = np.pad(norm_mel, ((0, 0), (0, pad_amount)), mode="constant")

    # Return shape [1, 80, time_frames]
    return np.expand_dims(norm_mel, axis=0).astype(np.float32)
