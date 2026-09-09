"""Unit tests for ASR result and audio data types."""

import pytest
import numpy as np
from tribetalk.asr.common.types import ASRResult, AudioData


class TestASRResult:
    """Test suite for ASRResult dataclass."""

    def test_hindi_result_creation(self) -> None:
        result = ASRResult(
            text="नमस्ते, आप कैसे हैं?",
            language="hi",
            processing_time_ms=120.5,
            audio_duration_ms=2400.0,
            is_final=True,
        )
        assert result.text == "नमस्ते, आप कैसे हैं?"
        assert result.language == "hi"
        assert result.processing_time_ms == 120.5
        assert result.audio_duration_ms == 2400.0
        assert result.is_final is True
        assert result.confidence is None

    def test_santali_result_creation(self) -> None:
        result = ASRResult(
            text="ᱡᱚᱦᱟᱨ, ᱪᱮᱫ ᱞᱮᱠᱟ ᱢᱮᱱᱟᱜ ᱵᱤᱱᱟ?",
            language="sat",
            processing_time_ms=180.2,
            audio_duration_ms=3100.0,
            is_final=True,
        )
        assert result.text == "ᱡᱚᱦᱟᱨ, ᱪᱮᱫ ᱞᱮᱠᱟ ᱢᱮᱱᱟᱜ ᱵᱤᱱᱟ?"
        assert result.language == "sat"
        assert result.is_final is True

    def test_result_with_confidence(self) -> None:
        result = ASRResult(
            text="परीक्षण",
            language="hi",
            processing_time_ms=50.0,
            audio_duration_ms=1000.0,
            confidence=0.92,
        )
        assert result.confidence == 0.92

    def test_rtf_calculation(self) -> None:
        result = ASRResult(
            text="test",
            language="hi",
            processing_time_ms=250.0,
            audio_duration_ms=1000.0,
        )
        assert result.rtf == pytest.approx(0.25)

    def test_rtf_zero_duration(self) -> None:
        result = ASRResult(
            text="",
            language="hi",
            processing_time_ms=10.0,
            audio_duration_ms=0.0,
        )
        assert result.rtf == 0.0

    def test_to_dict_serialization(self) -> None:
        result = ASRResult(
            text="भारत",
            language="hi",
            processing_time_ms=100.0,
            audio_duration_ms=500.0,
            is_final=True,
        )
        d = result.to_dict()
        assert isinstance(d, dict)
        assert d["text"] == "भारत"
        assert d["language"] == "hi"
        assert d["processing_time_ms"] == 100.0
        assert d["audio_duration_ms"] == 500.0
        assert d["is_final"] is True
        assert d["confidence"] is None
        assert d["rtf"] == 0.2

    def test_invalid_text_type_raises(self) -> None:
        with pytest.raises(TypeError):
            ASRResult(
                text=123,  # type: ignore
                language="hi",
                processing_time_ms=10.0,
                audio_duration_ms=100.0,
            )

    def test_empty_language_raises(self) -> None:
        with pytest.raises(ValueError):
            ASRResult(
                text="test",
                language="",
                processing_time_ms=10.0,
                audio_duration_ms=100.0,
            )

    def test_negative_processing_time_raises(self) -> None:
        with pytest.raises(ValueError):
            ASRResult(
                text="test",
                language="hi",
                processing_time_ms=-1.0,
                audio_duration_ms=100.0,
            )

    def test_negative_audio_duration_raises(self) -> None:
        with pytest.raises(ValueError):
            ASRResult(
                text="test",
                language="hi",
                processing_time_ms=10.0,
                audio_duration_ms=-100.0,
            )

    def test_invalid_confidence_range_raises(self) -> None:
        with pytest.raises(ValueError):
            ASRResult(
                text="test",
                language="hi",
                processing_time_ms=10.0,
                audio_duration_ms=100.0,
                confidence=1.5,
            )
        with pytest.raises(ValueError):
            ASRResult(
                text="test",
                language="hi",
                processing_time_ms=10.0,
                audio_duration_ms=100.0,
                confidence=-0.1,
            )

    def test_frozen_immutability(self) -> None:
        result = ASRResult(
            text="immutable",
            language="hi",
            processing_time_ms=10.0,
            audio_duration_ms=100.0,
        )
        with pytest.raises(AttributeError):
            result.text = "modified"  # type: ignore


class TestAudioData:
    """Test suite for AudioData container."""

    def test_audiodata_creation(self) -> None:
        samples = np.zeros(16000, dtype=np.float32)
        audio = AudioData(samples=samples, sample_rate=16000)
        assert len(audio.samples) == 16000
        assert audio.sample_rate == 16000
        assert audio.duration_ms == pytest.approx(1000.0)

    def test_multidimensional_samples_raises(self) -> None:
        stereo_samples = np.zeros((16000, 2), dtype=np.float32)
        with pytest.raises(ValueError, match="must be 1D"):
            AudioData(samples=stereo_samples)

    def test_auto_converts_float64_to_float32(self) -> None:
        samples = np.zeros(1600, dtype=np.float64)
        audio = AudioData(samples=samples, sample_rate=16000)
        assert audio.samples.dtype == np.float32
