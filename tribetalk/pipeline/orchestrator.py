"""End-to-End Bidirectional Speech Translation Pipeline Orchestrator for TribeTalk.

Coordinates Audio Input -> ASR -> Translation (NMT) -> TTS -> Audio Output
across Hindi (Devanagari) and Santali (Ol Chiki) with strict state machine
tracking and memory lifecycle enforcement.
"""

from __future__ import annotations

import logging
import threading
import time
from pathlib import Path
from typing import Callable, List, Optional, Union

import numpy as np

from tribetalk.asr.common.types import ASRResult, AudioData
from tribetalk.pipeline.types import (
    PipelineDirection,
    PipelineResult,
    PipelineState,
)
from tribetalk.resource_manager import GlobalModelResourceManager, MemoryMode
from tribetalk.translation.common.types import TranslationResult
from tribetalk.tts.common.types import TTSResult

logger = logging.getLogger(__name__)

StateListener = Callable[[PipelineState, PipelineState], None]


class TribeTalkPipeline:
    """Production-grade pipeline orchestrator for Hindi <-> Santali speech translation."""

    def __init__(
        self,
        resource_manager: Optional[GlobalModelResourceManager] = None,
        memory_mode: Union[MemoryMode, str] = MemoryMode.SEQUENTIAL,
    ) -> None:
        """Initialize the pipeline.
        
        Args:
            resource_manager: Optional custom GlobalModelResourceManager.
            memory_mode: Fallback memory mode if manager is instantiated internally.
        """
        self.resource_manager = resource_manager or GlobalModelResourceManager(
            mode=memory_mode
        )
        self._state = PipelineState.IDLE
        self._lock = threading.RLock()
        self._listeners: List[StateListener] = []

    # -------------------------------------------------------------------------
    # State Machine Management
    # -------------------------------------------------------------------------

    @property
    def state(self) -> PipelineState:
        """Current lifecycle state of the pipeline."""
        with self._lock:
            return self._state

    @property
    def is_idle(self) -> bool:
        """Check if pipeline is currently idle."""
        return self.state == PipelineState.IDLE

    def add_state_listener(self, listener: StateListener) -> None:
        """Register a callback for state transitions: callback(old_state, new_state)."""
        with self._lock:
            if listener not in self._listeners:
                self._listeners.append(listener)

    def remove_state_listener(self, listener: StateListener) -> None:
        """Remove a previously registered state transition callback."""
        with self._lock:
            if listener in self._listeners:
                self._listeners.remove(listener)

    def _set_state(self, new_state: PipelineState) -> None:
        """Internal thread-safe state transition with listener notifications."""
        with self._lock:
            old_state = self._state
            if old_state == new_state:
                return
            self._state = new_state
            logger.info(f"[Pipeline State] {old_state.value} -> {new_state.value}")

            # Notify listeners safely
            for listener in list(self._listeners):
                try:
                    listener(old_state, new_state)
                except Exception as e:
                    logger.warning(f"Error in pipeline state listener: {e}")

    # -------------------------------------------------------------------------
    # End-to-End Pipelines
    # -------------------------------------------------------------------------

    def process_speech(
        self,
        audio: Union[AudioData, np.ndarray, str, Path],
        direction: Union[str, PipelineDirection],
        synthesize_speech: bool = True,
    ) -> PipelineResult:
        """Execute full end-to-end speech translation.
        
        Workflow:
            1. Validate audio input
            2. State: ASR_PROCESSING -> Transcribe speech
            3. State: TRANSLATING -> Translate text
            4. State: SYNTHESIZING -> Synthesize speech (if enabled)
            5. State: COMPLETED -> IDLE
        
        Args:
            audio: Input audio data (16 kHz waveform, AudioData, or WAV path).
            direction: Translation direction ('hi_to_sat' or 'sat_to_hi').
            synthesize_speech: If True, synthesizes spoken audio at target.
        
        Returns:
            PipelineResult containing all intermediate outputs and latency.
        """
        start_time = time.perf_counter()
        dir_enum = PipelineDirection.from_string(direction)
        src_lang = dir_enum.source_lang
        tgt_lang = dir_enum.target_lang

        asr_res: Optional[ASRResult] = None
        nmt_res: Optional[TranslationResult] = None
        tts_res: Optional[TTSResult] = None

        try:
            # 1. Validation & ASR stage
            self._set_state(PipelineState.ASR_PROCESSING)
            asr_engine = self.resource_manager.acquire_asr(src_lang)
            asr_res = asr_engine.transcribe(audio)

            # Check for empty transcription
            src_text = asr_res.text.strip()
            if not src_text:
                total_latency = (time.perf_counter() - start_time) * 1000.0
                self._set_state(PipelineState.COMPLETED)
                return PipelineResult(
                    direction=dir_enum,
                    source_language=src_lang,
                    target_language=tgt_lang,
                    source_text="",
                    target_text="",
                    asr_result=asr_res,
                    total_latency_ms=total_latency,
                    success=True,
                )

            # 2. Translation stage
            self._set_state(PipelineState.TRANSLATING)
            nmt_engine = self.resource_manager.acquire_translation()
            nmt_res = nmt_engine.translate(
                text=src_text,
                source_language=src_lang,
                target_language=tgt_lang,
            )
            tgt_text = nmt_res.target_text.strip()

            # 3. Speech synthesis stage
            if synthesize_speech and tgt_text:
                self._set_state(PipelineState.SYNTHESIZING)
                tts_engine = self.resource_manager.acquire_tts(tgt_lang)
                tts_res = tts_engine.synthesize(tgt_text)

            total_latency = (time.perf_counter() - start_time) * 1000.0
            self._set_state(PipelineState.COMPLETED)

            return PipelineResult(
                direction=dir_enum,
                source_language=src_lang,
                target_language=tgt_lang,
                source_text=src_text,
                target_text=tgt_text,
                asr_result=asr_res,
                translation_result=nmt_res,
                tts_result=tts_res,
                total_latency_ms=total_latency,
                success=True,
            )

        except Exception as exc:
            total_latency = (time.perf_counter() - start_time) * 1000.0
            logger.exception(f"Pipeline failure during speech translation: {exc}")
            self._set_state(PipelineState.ERROR)
            return PipelineResult(
                direction=dir_enum,
                source_language=src_lang,
                target_language=tgt_lang,
                source_text=asr_res.text if asr_res else "",
                target_text=nmt_res.target_text if nmt_res else "",
                asr_result=asr_res,
                translation_result=nmt_res,
                tts_result=tts_res,
                total_latency_ms=total_latency,
                success=False,
                error_message=str(exc),
            )
        finally:
            self._set_state(PipelineState.IDLE)

    def process_text(
        self,
        text: str,
        direction: Union[str, PipelineDirection],
        synthesize_speech: bool = True,
    ) -> PipelineResult:
        """Execute text-to-speech / translation turn.
        
        Args:
            text: Input text (Devanagari or Ol Chiki).
            direction: Translation direction.
            synthesize_speech: If True, synthesizes target audio.
        """
        start_time = time.perf_counter()
        dir_enum = PipelineDirection.from_string(direction)
        src_lang = dir_enum.source_lang
        tgt_lang = dir_enum.target_lang

        nmt_res: Optional[TranslationResult] = None
        tts_res: Optional[TTSResult] = None

        try:
            clean_text = text.strip()
            if not clean_text:
                raise ValueError("Input text cannot be empty or whitespace")

            # 1. Translation stage
            self._set_state(PipelineState.TRANSLATING)
            nmt_engine = self.resource_manager.acquire_translation()
            nmt_res = nmt_engine.translate(
                text=clean_text,
                source_language=src_lang,
                target_language=tgt_lang,
            )
            tgt_text = nmt_res.target_text.strip()

            # 2. Synthesis stage
            if synthesize_speech and tgt_text:
                self._set_state(PipelineState.SYNTHESIZING)
                tts_engine = self.resource_manager.acquire_tts(tgt_lang)
                tts_res = tts_engine.synthesize(tgt_text)

            total_latency = (time.perf_counter() - start_time) * 1000.0
            self._set_state(PipelineState.COMPLETED)

            return PipelineResult(
                direction=dir_enum,
                source_language=src_lang,
                target_language=tgt_lang,
                source_text=clean_text,
                target_text=tgt_text,
                asr_result=None,
                translation_result=nmt_res,
                tts_result=tts_res,
                total_latency_ms=total_latency,
                success=True,
            )

        except Exception as exc:
            total_latency = (time.perf_counter() - start_time) * 1000.0
            logger.exception(f"Pipeline failure during text translation: {exc}")
            self._set_state(PipelineState.ERROR)
            return PipelineResult(
                direction=dir_enum,
                source_language=src_lang,
                target_language=tgt_lang,
                source_text=text,
                target_text=nmt_res.target_text if nmt_res else "",
                asr_result=None,
                translation_result=nmt_res,
                tts_result=tts_res,
                total_latency_ms=total_latency,
                success=False,
                error_message=str(exc),
            )
        finally:
            self._set_state(PipelineState.IDLE)

    # -------------------------------------------------------------------------
    # Granular Subsystem Shortcuts
    # -------------------------------------------------------------------------

    def transcribe_only(
        self,
        audio: Union[AudioData, np.ndarray, str, Path],
        language: str,
    ) -> ASRResult:
        """Run speech recognition only."""
        self._set_state(PipelineState.ASR_PROCESSING)
        try:
            engine = self.resource_manager.acquire_asr(language)
            return engine.transcribe(audio)
        finally:
            self._set_state(PipelineState.IDLE)

    def translate_only(
        self,
        text: str,
        source_language: str,
        target_language: str,
    ) -> TranslationResult:
        """Run text translation only."""
        self._set_state(PipelineState.TRANSLATING)
        try:
            engine = self.resource_manager.acquire_translation()
            return engine.translate(
                text=text,
                source_language=source_language,
                target_language=target_language,
            )
        finally:
            self._set_state(PipelineState.IDLE)

    def synthesize_only(
        self,
        text: str,
        language: str,
    ) -> TTSResult:
        """Run speech synthesis only."""
        self._set_state(PipelineState.SYNTHESIZING)
        try:
            engine = self.resource_manager.acquire_tts(language)
            return engine.synthesize(text)
        finally:
            self._set_state(PipelineState.IDLE)

    # -------------------------------------------------------------------------
    # Lifecycle & Maintenance Forwarding
    # -------------------------------------------------------------------------

    def release_all(self) -> None:
        """Release all model resources back to host memory."""
        self.resource_manager.release_all()

    def trim_memory(self) -> None:
        """Emergency memory trim."""
        self.resource_manager.trim_memory()

    def get_active_models(self) -> List[str]:
        """List currently loaded models."""
        return self.resource_manager.get_active_models()
