"""Unit tests for TTS result data structures and WAV serialization."""

from pathlib import Path
import pytest
import numpy as np
import soundfile as sf

from tribetalk.tts.common.types import TTSResult


class TestTTSResult:
    """Test suite for TTSResult dataclass."""

    def test_valid_creation(self) -> None:
        samples = np.zeros(16000, dtype=np.float32)
        res = TTSResult(
            audio=samples,
            sample_rate=16000,
            text="नमस्ते",
            language="hi",
            duration_ms=1000.0,
            processing_time_ms=250.0,
            model_name="Test-Model",
        )
        assert len(res.audio) == 16000
        assert res.sample_rate == 16000
        assert res.text == "नमस्ते"
        assert res.language == "hi"
        assert res.duration_ms == 1000.0
        assert res.processing_time_ms == 250.0
        assert res.rtf == pytest.approx(0.25)
        assert res.is_final is True

    def test_duration_auto_calculation(self) -> None:
        samples = np.zeros(32000, dtype=np.float32)  # 2.0 seconds at 16 kHz
        res = TTSResult(
            audio=samples,
            sample_rate=16000,
            text="test",
            language="sat",
            processing_time_ms=100.0,
        )
        assert res.duration_ms == pytest.approx(2000.0, rel=1e-2)

    def test_rtf_calculation_zero_duration(self) -> None:
        empty_samples = np.array([], dtype=np.float32)
        res = TTSResult(
            audio=empty_samples,
            sample_rate=16000,
            duration_ms=0.0,
            processing_time_ms=50.0,
        )
        assert res.rtf == 0.0

    def test_immutability(self) -> None:
        samples = np.zeros(1600, dtype=np.float32)
        res = TTSResult(audio=samples, text="sample")
        with pytest.raises(AttributeError):
            res.text = "modified"  # type: ignore

    def test_to_dict_serialization(self) -> None:
        samples = np.zeros(16000, dtype=np.float32)
        res = TTSResult(
            audio=samples,
            sample_rate=16000,
            text="ᱡᱚᱦᱟᱨ",
            language="sat",
            duration_ms=1000.0,
            processing_time_ms=80.0,
            model_name="Piper-ONNX",
        )
        d = res.to_dict()
        assert isinstance(d, dict)
        assert d["text"] == "ᱡᱚᱦᱟᱨ"
        assert d["language"] == "sat"
        assert d["sample_rate"] == 16000
        assert d["duration_ms"] == 1000.0
        assert d["processing_time_ms"] == 80.0
        assert d["rtf"] == 0.08
        assert d["audio_samples_count"] == 16000
        assert d["model_name"] == "Piper-ONNX"

    def test_save_wav_roundtrip(self, tmp_path: Path) -> None:
        wav_file = tmp_path / "test_out.wav"
        # Generate 1.0s of 440 Hz sine wave
        t = np.linspace(0, 1.0, 16000, endpoint=False)
        sine_audio = (0.5 * np.sin(2 * np.pi * 440.0 * t)).astype(np.float32)

        res = TTSResult(
            audio=sine_audio,
            sample_rate=16000,
            text="test sine",
            language="hi",
        )
        saved_path = res.save_wav(wav_file)
        assert saved_path.exists()
        assert saved_path.is_file()

        # Read back with soundfile and verify
        read_data, read_sr = sf.read(str(saved_path), dtype="float32")
        assert read_sr == 16000
        assert len(read_data) == 16000
        assert np.max(np.abs(read_data)) > 0.4

    def test_invalid_audio_dimension_raises(self) -> None:
        stereo_audio = np.zeros((16000, 2), dtype=np.float32)
        with pytest.raises(ValueError, match="must be 1D"):
            TTSResult(audio=stereo_audio)

    def test_negative_processing_time_raises(self) -> None:
        samples = np.zeros(1600, dtype=np.float32)
        with pytest.raises(ValueError, match="processing_time_ms"):
            TTSResult(audio=samples, processing_time_ms=-1.0)

    def test_empty_language_raises(self) -> None:
        samples = np.zeros(1600, dtype=np.float32)
        with pytest.raises(ValueError, match="language"):
            TTSResult(audio=samples, language="")

    def test_negative_sample_rate_raises(self) -> None:
        samples = np.zeros(1600, dtype=np.float32)
        with pytest.raises(ValueError, match="sample_rate"):
            TTSResult(audio=samples, sample_rate=-16000)
