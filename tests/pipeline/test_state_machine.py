"""Unit tests for PipelineState, PipelineDirection, PipelineResult, and state transitions."""

import pytest
from unittest.mock import MagicMock
from pathlib import Path

from tribetalk.pipeline.types import PipelineDirection, PipelineResult, PipelineState
from tribetalk.pipeline.orchestrator import TribeTalkPipeline
from tribetalk.resource_manager import GlobalModelResourceManager, MemoryMode
from tribetalk.asr.common.types import ASRResult
from tribetalk.translation.common.types import TranslationResult
from tribetalk.tts.common.types import TTSResult
import numpy as np


class TestPipelineTypes:
    """Test suite for pipeline types, enums, and result containers."""

    def test_pipeline_direction_from_string(self) -> None:
        assert PipelineDirection.from_string("hi_to_sat") == PipelineDirection.HINDI_TO_SANTALI
        assert PipelineDirection.from_string("hindi_to_santali") == PipelineDirection.HINDI_TO_SANTALI
        assert PipelineDirection.from_string("hi->sat") == PipelineDirection.HINDI_TO_SANTALI
        assert PipelineDirection.from_string("sat_to_hi") == PipelineDirection.SANTALI_TO_HINDI
        assert PipelineDirection.from_string("santali_to_hindi") == PipelineDirection.SANTALI_TO_HINDI
        assert PipelineDirection.from_string("sat->hi") == PipelineDirection.SANTALI_TO_HINDI

    def test_pipeline_direction_unsupported_raises(self) -> None:
        with pytest.raises(ValueError, match="Unsupported pipeline direction"):
            PipelineDirection.from_string("en_to_hi")

    def test_pipeline_direction_properties(self) -> None:
        d1 = PipelineDirection.HINDI_TO_SANTALI
        assert d1.source_lang == "hi"
        assert d1.target_lang == "sat"

        d2 = PipelineDirection.SANTALI_TO_HINDI
        assert d2.source_lang == "sat"
        assert d2.target_lang == "hi"

    def test_pipeline_result_immutability(self) -> None:
        res = PipelineResult(
            direction=PipelineDirection.HINDI_TO_SANTALI,
            source_language="hi",
            target_language="sat",
            source_text="नमस्ते",
            target_text="ᱡᱚᱦᱟᱨ",
            total_latency_ms=120.5,
            success=True,
        )
        assert res.direction == PipelineDirection.HINDI_TO_SANTALI
        assert res.source_text == "नमस्ते"
        assert res.target_text == "ᱡᱚᱦᱟᱨ"
        assert res.has_audio_output is False

        with pytest.raises(Exception):
            res.source_text = "new"  # type: ignore

    def test_pipeline_result_to_dict(self) -> None:
        res = PipelineResult(
            direction=PipelineDirection.SANTALI_TO_HINDI,
            source_language="sat",
            target_language="hi",
            source_text="ᱡᱚᱦᱟᱨ",
            target_text="नमस्ते",
            total_latency_ms=85.0,
            success=True,
        )
        d = res.to_dict()
        assert d["direction"] == "sat_to_hi"
        assert d["source_language"] == "sat"
        assert d["target_language"] == "hi"
        assert d["success"] is True
        assert d["total_latency_ms"] == 85.0


class TestPipelineStateTransitions:
    """Test suite for TribeTalkPipeline state machine and listeners."""

    def test_state_listener_transitions_during_process_text(self) -> None:
        recorded_transitions = []

        def listener(old_state: PipelineState, new_state: PipelineState) -> None:
            recorded_transitions.append((old_state, new_state))

        mock_nmt = MagicMock()
        mock_nmt.is_loaded = True
        mock_nmt.translate.return_value = TranslationResult(
            source_text="नमस्ते",
            target_text="ᱡᱚᱦᱟᱨ",
            source_language="hi",
            target_language="sat",
            processing_time_ms=15.0,
            model_name="mock_nmt",
        )

        mock_tts = MagicMock()
        mock_tts.is_loaded = True
        mock_tts.synthesize.return_value = TTSResult(
            audio=np.zeros(16000, dtype=np.float32),
            sample_rate=16000,
            text="ᱡᱚᱦᱟᱨ",
            language="sat",
            duration_ms=1000.0,
            processing_time_ms=50.0,
            model_name="mock_tts",
        )

        mgr = GlobalModelResourceManager(
            mode=MemoryMode.CACHED,
            translation_factory=lambda **kw: mock_nmt,
            tts_factory=lambda lang, **kw: mock_tts,
        )

        pipeline = TribeTalkPipeline(resource_manager=mgr)
        pipeline.add_state_listener(listener)

        result = pipeline.process_text("नमस्ते", direction="hi_to_sat", synthesize_speech=True)

        assert result.success is True
        assert result.has_audio_output is True
        assert result.target_text == "ᱡᱚᱦᱟᱨ"
        assert pipeline.is_idle is True

        # Check recorded sequence: IDLE -> TRANSLATING -> SYNTHESIZING -> COMPLETED -> IDLE
        states_sequence = [t[1] for t in recorded_transitions]
        assert states_sequence == [
            PipelineState.TRANSLATING,
            PipelineState.SYNTHESIZING,
            PipelineState.COMPLETED,
            PipelineState.IDLE,
        ]

    def test_pipeline_handles_exception_and_transitions_to_error(self) -> None:
        mock_nmt = MagicMock()
        mock_nmt.is_loaded = True
        mock_nmt.translate.side_effect = RuntimeError("Translation engine crashed")

        mgr = GlobalModelResourceManager(
            mode=MemoryMode.CACHED,
            translation_factory=lambda **kw: mock_nmt,
        )
        pipeline = TribeTalkPipeline(resource_manager=mgr)

        result = pipeline.process_text("नमस्ते", direction="hi_to_sat")

        assert result.success is False
        assert "Translation engine crashed" in (result.error_message or "")
        assert pipeline.is_idle is True  # Finally block resets to IDLE
