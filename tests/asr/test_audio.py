"""Unit tests for shared audio pipeline."""

import pytest
import numpy as np
import soundfile as sf
from pathlib import Path
from tribetalk.asr.common.audio import (
    TARGET_SAMPLE_RATE,
    AudioLoadingError,
    load_and_preprocess_audio,
    save_temp_wav,
)
from tribetalk.asr.common.types import AudioData


class TestAudioPipeline:
    """Test suite for shared audio loading, conversion, and resampling."""

    def test_load_mono_16k(self, mono_16k_wav: Path) -> None:
        audio = load_and_preprocess_audio(mono_16k_wav)
        assert isinstance(audio, AudioData)
        assert audio.sample_rate == TARGET_SAMPLE_RATE
        assert audio.samples.ndim == 1
        assert audio.samples.dtype == np.float32
        assert audio.original_channels == 1
        assert audio.original_sample_rate == 16000
        assert len(audio.samples) == 16000
        assert audio.duration_ms == pytest.approx(1000.0, rel=1e-2)

    def test_stereo_to_mono_and_resampling(self, stereo_44k_wav: Path) -> None:
        audio = load_and_preprocess_audio(stereo_44k_wav)
        assert audio.sample_rate == TARGET_SAMPLE_RATE
        assert audio.samples.ndim == 1
        assert audio.original_channels == 2
        assert audio.original_sample_rate == 44100
        # 1.5 seconds at 16000 Hz = 24000 samples
        assert len(audio.samples) == pytest.approx(24000, abs=10)
        assert audio.duration_ms == pytest.approx(1500.0, rel=1e-2)

    def test_resample_upsample_8k_to_16k(self, mono_8k_wav: Path) -> None:
        audio = load_and_preprocess_audio(mono_8k_wav)
        assert audio.sample_rate == TARGET_SAMPLE_RATE
        assert audio.original_sample_rate == 8000
        # 2.0 seconds at 16000 Hz = 32000 samples
        assert len(audio.samples) == pytest.approx(32000, abs=10)
        assert audio.duration_ms == pytest.approx(2000.0, rel=1e-2)

    def test_resample_downsample_48k_to_16k(self, mono_48k_wav: Path) -> None:
        audio = load_and_preprocess_audio(mono_48k_wav)
        assert audio.sample_rate == TARGET_SAMPLE_RATE
        assert audio.original_sample_rate == 48000
        # 0.8 seconds at 16000 Hz = 12800 samples
        assert len(audio.samples) == pytest.approx(12800, abs=10)
        assert audio.duration_ms == pytest.approx(800.0, rel=1e-2)

    def test_amplitude_bounds(self, stereo_44k_wav: Path) -> None:
        audio = load_and_preprocess_audio(stereo_44k_wav)
        assert np.max(np.abs(audio.samples)) <= 1.0

    def test_nonexistent_file_raises_filenotfound(self, tmp_path: Path) -> None:
        missing_path = tmp_path / "does_not_exist.wav"
        with pytest.raises(FileNotFoundError, match="does not exist"):
            load_and_preprocess_audio(missing_path)

    def test_empty_file_raises_audioloadingerror(self, empty_wav: Path) -> None:
        with pytest.raises(AudioLoadingError, match="empty"):
            load_and_preprocess_audio(empty_wav)

    def test_corrupt_file_raises_audioloadingerror(self, corrupt_wav: Path) -> None:
        with pytest.raises(AudioLoadingError, match="Failed to read or decode"):
            load_and_preprocess_audio(corrupt_wav)

    def test_save_temp_wav(self, mono_16k_wav: Path, tmp_path: Path) -> None:
        audio = load_and_preprocess_audio(mono_16k_wav)
        out_path = tmp_path / "persisted_test.wav"
        saved = save_temp_wav(audio, output_path=out_path)

        assert saved.exists()
        info = sf.info(str(saved))
        assert info.samplerate == 16000
        assert info.channels == 1
        assert info.subtype == "PCM_16"
        assert info.duration == pytest.approx(1.0, rel=1e-2)
