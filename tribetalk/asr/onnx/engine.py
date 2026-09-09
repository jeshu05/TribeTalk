"""Production ONNX Runtime ASR engine for IndicConformer (Hindi & Santali)."""

from __future__ import annotations

import logging
from pathlib import Path
import time
from typing import Optional, Union

import numpy as np

from tribetalk.asr.common.audio import load_and_preprocess_audio
from tribetalk.asr.common.interface import ASRInterface
from tribetalk.asr.common.types import ASRResult, AudioData
from tribetalk.asr.onnx.features import extract_conformer_features
from tribetalk.asr.onnx.model import IndicConformerONNXModel

logger = logging.getLogger(__name__)


class IndicConformerONNX(ASRInterface):
    """Production-grade ONNX Runtime speech recognizer for Hindi and Santali."""

    def __init__(
        self,
        language: str,
        model_path_or_repo: Optional[Union[str, Path]] = None,
        use_int8: bool = True,
        lazy_load: bool = True,
        **kwargs,
    ) -> None:
        """Initialize IndicConformer ONNX engine.

        Args:
            language: 'hi' or 'sat'.
            model_path_or_repo: Local directory or Hugging Face repo ID.
            use_int8: If True, uses INT8 quantized graph (131.3 MB).
            lazy_load: If False, loads model immediately.
        """
        self._model_mgr = IndicConformerONNXModel(
            language=language,
            model_path_or_repo=model_path_or_repo,
            use_int8=use_int8,
        )
        if not lazy_load:
            self._model_mgr.load()

    @property
    def language(self) -> str:
        """Language code ('hi' or 'sat')."""
        return self._model_mgr.language

    @property
    def is_loaded(self) -> bool:
        """Whether model is loaded in memory."""
        return self._model_mgr.is_loaded

    def load_model(self) -> None:
        """Explicitly load model into memory."""
        self._model_mgr.load()

    def unload_model(self) -> None:
        """Explicitly release model from memory."""
        self._model_mgr.unload()

    def transcribe(
        self, audio: Union[str, Path, AudioData, np.ndarray]
    ) -> ASRResult:
        """Transcribe speech audio into vernacular text (Devanagari or Ol Chiki).

        Args:
            audio: Audio file path, AudioData object, or 1D float32 numpy array.

        Returns:
            ASRResult containing transcribed text, duration, and latency.
        """
        if isinstance(audio, (str, Path)):
            audio_data = load_and_preprocess_audio(audio)
            samples = audio_data.samples
            sr = audio_data.sample_rate
            duration_ms = audio_data.duration_ms
        elif isinstance(audio, AudioData):
            samples = audio.samples
            sr = audio.sample_rate
            duration_ms = audio.duration_ms
        elif isinstance(audio, np.ndarray):
            samples = audio.astype(np.float32)
            sr = 16000
            duration_ms = (len(samples) / sr) * 1000.0
        else:
            raise TypeError(f"Unsupported audio input type: {type(audio)}")

        t0 = time.perf_counter()

        session, vocab, blank_id = self._model_mgr.get_session_and_vocab()

        # 1. Extract 80-channel log-mel features with per-feature normalization
        feats = extract_conformer_features(samples, sample_rate=sr)
        length = np.array([feats.shape[2]], dtype=np.int64)

        # 2. Run ONNX acoustic model inference
        outputs = session.run(
            None,
            {
                "audio_signal": feats,
                "length": length,
            },
        )
        logprobs = outputs[0][0]  # shape [time_steps, vocab_size]

        # 3. Greedy CTC decoding with blank-folding
        preds = np.argmax(logprobs, axis=-1)

        collapsed = []
        prev = None
        for p in preds:
            if p != prev:
                if p != blank_id:
                    collapsed.append(p)
                prev = p

        raw_text = "".join(vocab.get(p, "") for p in collapsed)
        clean_text = raw_text.replace("\u2581", " ").strip()

        latency_ms = (time.perf_counter() - t0) * 1000.0

        return ASRResult(
            text=clean_text,
            language=self.language,
            processing_time_ms=round(latency_ms, 2),
            audio_duration_ms=round(duration_ms, 2),
            is_final=True,
        )
