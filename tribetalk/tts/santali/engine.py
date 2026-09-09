"""Santali TTS speech synthesis engine implementing TTSInterface."""

from pathlib import Path
from typing import Optional, Union, List
import time
import numpy as np
import logging

from tribetalk.tts.common.interface import TTSInterface
from tribetalk.tts.common.types import TTSResult
from tribetalk.tts.santali.model import SantaliTTSModel

logger = logging.getLogger(__name__)


class SantaliTTS(TTSInterface):
    """Santali Text-to-Speech engine producing 16 kHz audio via Piper VITS ONNX."""

    def __init__(
        self,
        model_path_or_repo: Optional[Union[str, Path]] = None,
        providers: Optional[List[str]] = None,
        lazy_load: bool = True,
    ) -> None:
        """Initialize Santali TTS engine.

        Args:
            model_path_or_repo: Local directory path or Hugging Face repo ID.
            providers: ONNX Runtime execution providers.
            lazy_load: If False, loads model immediately upon initialization.
        """
        self._model_mgr = SantaliTTSModel(
            model_path_or_repo=model_path_or_repo,
            providers=providers,
        )
        if not lazy_load:
            self._model_mgr.load()

    @property
    def language(self) -> str:
        """Language code ('sat')."""
        return "sat"

    @property
    def sample_rate(self) -> int:
        """Sampling rate in Hz (16,000 Hz)."""
        return self._model_mgr.sample_rate

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
        """Synthesize Santali Ol Chiki text into natural 16 kHz audio.

        Args:
            text: Input Santali sentence in Ol Chiki script.

        Returns:
            TTSResult containing the 16 kHz audio waveform and timing metrics.

        Raises:
            ValueError: If text is empty.
        """
        cleaned = text.strip()
        if not cleaned:
            raise ValueError("Input text cannot be empty for speech synthesis.")

        session, pid_map, scales, sr = self._model_mgr.get_session_and_config()

        t0 = time.perf_counter()

        # 1. Tokenize Ol Chiki characters into VITS padded sequence
        # BOS = 1, EOS = 2, PAD = 0
        bos = pid_map.get("^", [1])[0]
        eos = pid_map.get("$", [2])[0]
        pad = pid_map.get("_", [0])[0]

        seq: List[int] = [bos]
        for ch in cleaned:
            if ch in pid_map:
                seq.append(pad)
                seq.append(pid_map[ch][0])
            elif ch == " ":
                # Space character
                space_id = pid_map.get(" ", [3])[0]
                seq.append(pad)
                seq.append(space_id)

        seq.append(pad)
        seq.append(eos)

        # 2. Prepare ONNX input tensors
        input_ids = np.array([seq], dtype=np.int64)
        input_lengths = np.array([len(seq)], dtype=np.int64)

        # 3. Run ONNX inference
        outputs = session.run(
            None,
            {
                "input": input_ids,
                "input_lengths": input_lengths,
                "scales": scales,
            },
        )

        # 4. Extract and normalize waveform
        raw_audio = outputs[0].squeeze().astype(np.float32)

        # Amplitude protection
        max_amp = np.max(np.abs(raw_audio)) if len(raw_audio) > 0 else 0.0
        if max_amp > 1.0:
            raw_audio = raw_audio / max_amp

        latency_ms = (time.perf_counter() - t0) * 1000.0
        duration_ms = (len(raw_audio) / sr) * 1000.0

        return TTSResult(
            audio=raw_audio,
            sample_rate=sr,
            text=cleaned,
            language=self.language,
            duration_ms=round(duration_ms, 2),
            processing_time_ms=round(latency_ms, 2),
            model_name="Piper-Santali-VITS-ONNX",
            is_final=True,
        )
