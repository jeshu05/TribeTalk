"""Pytest fixtures for TribeTalk ASR tests."""

import pytest
import numpy as np
import soundfile as sf
from pathlib import Path
from typing import Generator, Tuple


def create_synthetic_sine_wav(
    file_path: Path,
    duration_s: float = 1.0,
    sample_rate: int = 16000,
    frequency: float = 440.0,
    channels: int = 1,
) -> Path:
    """Helper to generate a clean synthetic WAV file with sine wave audio."""
    num_samples = int(duration_s * sample_rate)
    t = np.linspace(0, duration_s, num_samples, endpoint=False, dtype=np.float32)
    sine = 0.5 * np.sin(2 * np.pi * frequency * t)

    if channels == 1:
        audio = sine
    else:
        # Create distinct channel signals to verify downmixing
        audio = np.stack([sine * (1.0 + 0.2 * ch) for ch in range(channels)], axis=-1)

    sf.write(str(file_path), audio, sample_rate, subtype="PCM_16")
    return file_path


@pytest.fixture
def mono_16k_wav(tmp_path: Path) -> Path:
    """A 1-second 16 kHz mono WAV file."""
    path = tmp_path / "mono_16k.wav"
    return create_synthetic_sine_wav(path, duration_s=1.0, sample_rate=16000, channels=1)


@pytest.fixture
def stereo_44k_wav(tmp_path: Path) -> Path:
    """A 1.5-second 44.1 kHz stereo WAV file."""
    path = tmp_path / "stereo_44k.wav"
    return create_synthetic_sine_wav(path, duration_s=1.5, sample_rate=44100, channels=2)


@pytest.fixture
def mono_8k_wav(tmp_path: Path) -> Path:
    """A 2-second 8 kHz mono WAV file."""
    path = tmp_path / "mono_8k.wav"
    return create_synthetic_sine_wav(path, duration_s=2.0, sample_rate=8000, channels=1)


@pytest.fixture
def mono_48k_wav(tmp_path: Path) -> Path:
    """A 0.8-second 48 kHz mono WAV file."""
    path = tmp_path / "mono_48k.wav"
    return create_synthetic_sine_wav(path, duration_s=0.8, sample_rate=48000, channels=1)


@pytest.fixture
def empty_wav(tmp_path: Path) -> Path:
    """A 0-byte file with .wav extension."""
    path = tmp_path / "empty.wav"
    path.touch()
    return path


@pytest.fixture
def corrupt_wav(tmp_path: Path) -> Path:
    """A corrupted file containing non-audio random text."""
    path = tmp_path / "corrupt.wav"
    path.write_bytes(b"NOT_A_REAL_RIFF_HEADER_RANDOM_CORRUPT_BYTES_DATA")
    return path
