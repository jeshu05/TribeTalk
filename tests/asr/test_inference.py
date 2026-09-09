"""Integration tests for genuine Hindi and Santali ASR model inference."""

import pytest
from pathlib import Path
from tribetalk.asr.hindi import HindiASR
from tribetalk.asr.santali import SantaliASR
from tribetalk.asr import get_asr
from tribetalk.asr.common.types import ASRResult


@pytest.mark.integration
class TestHindiInference:
    """Integration test suite for IndicConformer Hindi ASR."""

    def test_hindi_inference_end_to_end(self, mono_16k_wav: Path) -> None:
        recognizer = HindiASR(lazy_load=False)
        assert recognizer.is_loaded is True
        assert recognizer.language == "hi"

        result = recognizer.transcribe(mono_16k_wav)

        assert isinstance(result, ASRResult)
        assert result.language == "hi"
        assert isinstance(result.text, str)
        assert result.audio_duration_ms == pytest.approx(1000.0, rel=1e-2)
        assert result.processing_time_ms > 0
        assert result.is_final is True

    def test_hindi_inference_via_factory(self, mono_16k_wav: Path) -> None:
        recognizer = get_asr("hi", lazy_load=False)
        assert isinstance(recognizer, HindiASR)
        result = recognizer.transcribe(mono_16k_wav)
        assert result.language == "hi"
        assert result.is_final is True

    def test_hindi_successive_transcriptions_reuse_model(
        self, mono_16k_wav: Path
    ) -> None:
        recognizer = HindiASR(lazy_load=False)
        res1 = recognizer.transcribe(mono_16k_wav)
        res2 = recognizer.transcribe(mono_16k_wav)
        assert res1.language == "hi"
        assert res2.language == "hi"


@pytest.mark.integration
class TestSantaliInference:
    """Integration test suite for fine-tuned Whisper Santali ASR."""

    def test_santali_inference_end_to_end(self, mono_16k_wav: Path) -> None:
        recognizer = SantaliASR(lazy_load=False)
        assert recognizer.is_loaded is True
        assert recognizer.language == "sat"

        result = recognizer.transcribe(mono_16k_wav)

        assert isinstance(result, ASRResult)
        assert result.language == "sat"
        assert isinstance(result.text, str)
        assert result.audio_duration_ms == pytest.approx(1000.0, rel=1e-2)
        assert result.processing_time_ms > 0
        assert result.is_final is True

    def test_santali_inference_via_factory(self, mono_16k_wav: Path) -> None:
        recognizer = get_asr("sat", lazy_load=False)
        assert isinstance(recognizer, SantaliASR)
        result = recognizer.transcribe(mono_16k_wav)
        assert result.language == "sat"
        assert result.is_final is True

    def test_santali_successive_transcriptions_reuse_model(
        self, mono_16k_wav: Path
    ) -> None:
        recognizer = SantaliASR(lazy_load=False)
        res1 = recognizer.transcribe(mono_16k_wav)
        res2 = recognizer.transcribe(mono_16k_wav)
        assert res1.language == "sat"
        assert res2.language == "sat"
