"""Unit and integration tests for production IndicConformer ONNX ASR engines."""

from pathlib import Path
from typing import Tuple
import numpy as np
import pytest

from tribetalk.asr import get_asr
from tribetalk.asr.common.interface import ASRInterface
from tribetalk.asr.common.types import ASRResult
from tribetalk.asr.onnx.engine import IndicConformerONNX
from tribetalk.asr.onnx.features import extract_conformer_features
from tribetalk.translation.normalization import TextNormalizer


class TestIndicConformerONNXFeatures:
    """Test suite for conformer 80-channel log-mel feature extraction."""

    def test_feature_shape_and_padding(self) -> None:
        audio = np.zeros(16000, dtype=np.float32)
        feats = extract_conformer_features(audio, sample_rate=16000)
        assert feats.ndim == 3
        assert feats.shape[0] == 1
        assert feats.shape[1] == 80
        # Time dimension must be multiple of 16
        assert feats.shape[2] % 16 == 0

    def test_feature_normalization(self) -> None:
        # Create non-zero signal
        t = np.linspace(0, 1.0, 16000, endpoint=False, dtype=np.float32)
        audio = 0.5 * np.sin(2 * np.pi * 440.0 * t)
        feats = extract_conformer_features(audio, sample_rate=16000)
        # Across time frames, mean of each channel should be roughly zero
        mean_per_channel = np.mean(feats[0], axis=1)
        assert np.all(np.abs(mean_per_channel) < 0.5)


@pytest.mark.integration
class TestIndicConformerONNXEngines:
    """Integration test suite for Hindi and Santali ONNX ASR engines."""

    def test_hindi_onnx_engine_contract(self) -> None:
        engine = IndicConformerONNX(language="hi", lazy_load=True)
        assert isinstance(engine, ASRInterface)
        assert engine.language == "hi"
        assert engine.is_loaded is False

        engine.load_model()
        assert engine.is_loaded is True

        engine.unload_model()
        assert engine.is_loaded is False

    def test_santali_onnx_engine_contract(self) -> None:
        engine = IndicConformerONNX(language="sat", lazy_load=True)
        assert isinstance(engine, ASRInterface)
        assert engine.language == "sat"
        assert engine.is_loaded is False

        engine.load_model()
        assert engine.is_loaded is True

        engine.unload_model()
        assert engine.is_loaded is False

    def test_hindi_onnx_transcription(self, mono_16k_wav: Path) -> None:
        engine = IndicConformerONNX(language="hi", lazy_load=False)
        result = engine.transcribe(mono_16k_wav)
        assert isinstance(result, ASRResult)
        assert result.language == "hi"
        assert result.processing_time_ms > 0
        assert result.audio_duration_ms > 0
        assert result.is_final is True

    def test_santali_onnx_transcription(self, mono_16k_wav: Path) -> None:
        engine = IndicConformerONNX(language="sat", lazy_load=False)
        result = engine.transcribe(mono_16k_wav)
        assert isinstance(result, ASRResult)
        assert result.language == "sat"
        assert result.processing_time_ms > 0
        assert result.is_final is True

    def test_factory_function_onnx_backend(self) -> None:
        hi_asr = get_asr("hi", backend="onnx", lazy_load=True)
        assert isinstance(hi_asr, IndicConformerONNX)
        assert hi_asr.language == "hi"

        sat_asr = get_asr("sat", backend="onnx", lazy_load=True)
        assert isinstance(sat_asr, IndicConformerONNX)
        assert sat_asr.language == "sat"
