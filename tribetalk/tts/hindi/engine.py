"""Hindi TTS speech synthesis engine implementing TTSInterface via ONNX Runtime."""

from __future__ import annotations

import logging
from pathlib import Path
import time
from typing import Optional, Union

import numpy as np

from tribetalk.tts.common.interface import TTSInterface
from tribetalk.tts.common.types import TTSResult
from tribetalk.tts.hindi.model import HindiTTSModel

logger = logging.getLogger(__name__)


class HindiTTS(TTSInterface):
    """Hindi Text-to-Speech engine producing 16 kHz audio via pure ONNX Runtime."""

    def __init__(
        self,
        model_path_or_repo: Optional[Union[str, Path]] = None,
        use_quantized: bool = False,
        lazy_load: bool = True,
        **kwargs,
    ) -> None:
        """Initialize Hindi TTS engine.

        Args:
            model_path_or_repo: Local model path or Hugging Face repo ID.
            use_quantized: If True, uses the INT8 quantized model graph.
            lazy_load: If False, loads model immediately upon initialization.
        """
        self._model_mgr = HindiTTSModel(
            model_path_or_repo=model_path_or_repo,
            use_quantized=use_quantized,
        )
        if not lazy_load:
            self._model_mgr.load()

    @property
    def language(self) -> str:
        """Language code ('hi')."""
        return "hi"

    @property
    def sample_rate(self) -> int:
        """Sampling rate in Hz (16,000 Hz standard)."""
        return 16000

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

    def synthesize(self, text: str) -> TTSResult:
        """Synthesize Hindi Devanagari text into natural 16 kHz audio via ONNX Runtime.

        Args:
            text: Input Hindi sentence in Devanagari script.

        Returns:
            TTSResult containing 16 kHz audio waveform and timing metrics.

        Raises:
            ValueError: If text is empty or purely whitespace.
        """
        cleaned = text.strip()
        if not cleaned:
            raise ValueError("Input text cannot be empty for speech synthesis.")

        session, vocab = self._model_mgr.get_session_and_vocab()

        t0 = time.perf_counter()

        # Pure character-level VITS tokenization with inter-token blank padding (0)
        # Matches AutoTokenizer from Xenova/mms-tts-hin exactly
        unk_token = vocab.get("<unk>", 0)
        token_ids = [0]
        for ch in cleaned.lower():
            if ch in vocab:
                token_ids.extend([vocab[ch], 0])
            elif unk_token:
                token_ids.extend([unk_token, 0])

        if len(token_ids) <= 1:
            token_ids = [0, unk_token, 0]

        input_ids = np.array([token_ids], dtype=np.int64)
        attention_mask = np.ones_like(input_ids, dtype=np.int64)

        # Run ONNX inference
        outputs = session.run(
            None,
            {
                "input_ids": input_ids,
                "attention_mask": attention_mask,
            },
        )

        # Output waveform is at index 0, shaped [1, n_samples]
        raw_waveform = outputs[0][0].astype(np.float32)

        # Peak normalization to [-1.0, 1.0] if necessary
        max_val = np.max(np.abs(raw_waveform))
        if max_val > 1.0:
            audio_arr = raw_waveform / max_val
        else:
            audio_arr = raw_waveform

        latency_ms = (time.perf_counter() - t0) * 1000.0
        duration_ms = (len(audio_arr) / self.sample_rate) * 1000.0

        return TTSResult(
            audio=audio_arr,
            sample_rate=self.sample_rate,
            text=cleaned,
            language=self.language,
            duration_ms=round(duration_ms, 2),
            processing_time_ms=round(latency_ms, 2),
            model_name="mms-tts-hin-onnx",
        )
