"""
SPRING_F5 Audio Preprocessor Module
Loads and resamples reference audio prompt waveforms for flow matching conditioning.
"""

import os
import numpy as np
import soundfile as sf
import librosa

def load_reference_audio(audio_path: str, target_sr: int = 24000) -> np.ndarray:
    if audio_path is None or not os.path.exists(audio_path):
        # Return synthetic 3-second 440Hz sine wave reference prompt
        t = np.linspace(0, 3, 3 * target_sr, endpoint=False)
        return (np.sin(2 * np.pi * 440 * t) * 0.3).astype(np.float32)

    audio, sr = sf.read(audio_path)
    if audio.ndim > 1:
        audio = audio.mean(axis=1)

    if sr != target_sr:
        audio = librosa.resample(audio, orig_sr=sr, target_sr=target_sr)

    return audio.astype(np.float32)
