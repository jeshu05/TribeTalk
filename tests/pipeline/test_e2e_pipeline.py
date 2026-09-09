"""End-to-end integration tests for bidirectional Hindi <-> Santali speech translation."""

from pathlib import Path
from typing import Tuple
import pytest
import numpy as np

from tribetalk.pipeline.orchestrator import TribeTalkPipeline
from tribetalk.pipeline.types import PipelineDirection, PipelineResult
from tribetalk.resource_manager import GlobalModelResourceManager, MemoryMode
from tribetalk.translation.normalization import TextNormalizer


@pytest.mark.integration
class TestEndToEndPipeline:
    """Integration test suite executing genuine end-to-end translation turns."""

    def test_e2e_text_translation_and_synthesis_hindi_to_santali(self) -> None:
        """Test full Text (Hindi) -> NMT -> Text (Santali Ol Chiki) -> TTS (16kHz Audio)."""
        pipeline = TribeTalkPipeline(memory_mode=MemoryMode.CACHED)

        hindi_text = "माँ, चलो कल एक फिल्म देखने चलते हैं।"
        result = pipeline.process_text(
            text=hindi_text,
            direction="hi_to_sat",
            synthesize_speech=True,
        )

        assert isinstance(result, PipelineResult)
        assert result.success is True
        assert result.direction == PipelineDirection.HINDI_TO_SANTALI
        assert result.source_language == "hi"
        assert result.target_language == "sat"
        assert result.source_text == hindi_text

        # Target text must contain Ol Chiki characters
        assert TextNormalizer.contains_ol_chiki(result.target_text)

        # TTS output must be valid 16 kHz audio
        assert result.has_audio_output is True
        assert result.tts_result is not None
        assert result.tts_result.sample_rate == 16000
        assert len(result.tts_result.audio) > 0
        assert result.total_latency_ms > 0

    def test_e2e_text_translation_and_synthesis_santali_to_hindi(self) -> None:
        """Test full Text (Santali Ol Chiki) -> NMT -> Text (Hindi Devanagari) -> TTS (16kHz Audio)."""
        pipeline = TribeTalkPipeline(memory_mode=MemoryMode.CACHED)

        santali_text = "ᱜᱚ, ᱜᱟᱯᱟ ᱢᱚᱵᱷᱤ ᱧᱮᱞᱵᱚᱱ ᱪᱚᱞᱚᱜᱼᱟ ᱾"
        result = pipeline.process_text(
            text=santali_text,
            direction="sat_to_hi",
            synthesize_speech=True,
        )

        assert isinstance(result, PipelineResult)
        assert result.success is True
        assert result.direction == PipelineDirection.SANTALI_TO_HINDI
        assert result.source_language == "sat"
        assert result.target_language == "hi"
        assert result.source_text == santali_text

        # Target text must contain Devanagari characters
        assert TextNormalizer.contains_devanagari(result.target_text)

        # TTS output must be valid 16 kHz audio
        assert result.has_audio_output is True
        assert result.tts_result is not None
        assert result.tts_result.sample_rate == 16000
        assert len(result.tts_result.audio) > 0

    def test_e2e_hindi_speech_to_santali_speech_indicvoices(
        self, hindi_eval_sample: Tuple[Path, str]
    ) -> None:
        """Test complete speech turn: Hindi Audio (IndicVoices) -> ASR -> NMT -> Santali Audio."""
        wav_path, ref_text = hindi_eval_sample
        pipeline = TribeTalkPipeline(memory_mode=MemoryMode.CACHED)

        result = pipeline.process_speech(
            audio=wav_path,
            direction="hi_to_sat",
            synthesize_speech=True,
        )

        assert result.success is True
        assert result.direction == PipelineDirection.HINDI_TO_SANTALI
        assert result.asr_result is not None
        assert len(result.source_text) > 0

        # NMT target text in Ol Chiki
        assert TextNormalizer.contains_ol_chiki(result.target_text)

        # Santali synthesized speech
        assert result.has_audio_output is True
        assert result.tts_result is not None
        assert result.tts_result.sample_rate == 16000
        assert len(result.tts_result.audio) > 0

    def test_e2e_santali_speech_to_hindi_speech_indicvoices(
        self, santali_eval_sample: Tuple[Path, str]
    ) -> None:
        """Test complete speech turn: Santali Audio (IndicVoices) -> ASR -> NMT -> Hindi Audio."""
        wav_path, ref_text = santali_eval_sample
        pipeline = TribeTalkPipeline(memory_mode=MemoryMode.CACHED)

        result = pipeline.process_speech(
            audio=wav_path,
            direction="sat_to_hi",
            synthesize_speech=True,
        )

        assert result.success is True
        assert result.direction == PipelineDirection.SANTALI_TO_HINDI
        assert result.asr_result is not None
        assert len(result.source_text) > 0

        # NMT target text in Devanagari
        assert TextNormalizer.contains_devanagari(result.target_text)

        # Hindi synthesized speech
        assert result.has_audio_output is True
        assert result.tts_result is not None
        assert result.tts_result.sample_rate == 16000
        assert len(result.tts_result.audio) > 0

    def test_e2e_sequential_memory_mode_releases_models(
        self, synthetic_16k_wav: Path
    ) -> None:
        """Verify that running a turn in SEQUENTIAL mode frees previous models."""
        pipeline = TribeTalkPipeline(memory_mode=MemoryMode.SEQUENTIAL)

        result = pipeline.process_text(
            text="नमस्ते",
            direction="hi_to_sat",
            synthesize_speech=True,
        )

        assert result.success is True
        # In SEQUENTIAL mode, models are unloaded when next is acquired or turn finishes
        # At most 1 model should be loaded
        active = pipeline.get_active_models()
        assert len(active) <= 1

        # Clean release
        pipeline.release_all()
        assert len(pipeline.get_active_models()) == 0
