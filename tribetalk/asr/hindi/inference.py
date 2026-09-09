"""Hindi ASR inference engine implementing ASRInterface."""

from pathlib import Path
from typing import Optional, Union
import time
import os
import logging

from tribetalk.asr.common.interface import ASRInterface
from tribetalk.asr.common.types import ASRResult
from tribetalk.asr.common.audio import load_and_preprocess_audio, save_temp_wav
from tribetalk.asr.hindi.model import HindiASRModel

logger = logging.getLogger(__name__)


class HindiASR(ASRInterface):
    """Hindi speech recognizer using AI4Bharat IndicConformer."""

    def __init__(
        self,
        model_path_or_repo: Optional[Union[str, Path]] = None,
        device: Optional[str] = None,
        decoder: str = "ctc",
        lazy_load: bool = True,
    ) -> None:
        """Initialize Hindi ASR engine.

        Args:
            model_path_or_repo: Path to local .nemo file or HF repo ID.
            device: 'cuda', 'cpu', or None (auto-detect).
            decoder: 'ctc' or 'rnnt'.
            lazy_load: If False, loads model immediately upon initialization.
        """
        self._model_mgr = HindiASRModel(
            model_path_or_repo=model_path_or_repo,
            device=device,
            decoder=decoder,
        )
        if not lazy_load:
            self._model_mgr.load()

    @property
    def language(self) -> str:
        """Target language code ('hi')."""
        return "hi"

    @property
    def is_loaded(self) -> bool:
        """Check if model is currently loaded in memory."""
        return self._model_mgr.is_loaded

    def load_model(self) -> None:
        """Explicitly load model weights into memory."""
        self._model_mgr.load()

    def unload_model(self) -> None:
        """Explicitly unload model weights to release memory."""
        self._model_mgr.unload()

    def transcribe(self, audio_path: Union[str, Path]) -> ASRResult:
        """Transcribe Hindi speech from an audio file.

        Args:
            audio_path: Path to audio file.

        Returns:
            ASRResult containing Hindi Devanagari transcript and metrics.
        """
        # Step 1: Preprocess audio using shared pipeline
        audio_data = load_and_preprocess_audio(audio_path)

        # Step 2: Ensure model is loaded
        model = self._model_mgr.get_model()

        # Step 3: Write standard 16 kHz mono WAV for NeMo inference
        temp_wav = save_temp_wav(audio_data)

        try:
            start_time = time.perf_counter()

            # Step 4: Run inference
            # NeMo transcribe API accepts audio=[list_of_paths]
            raw_output = model.transcribe(
                audio=[str(temp_wav)],
                batch_size=1,
                verbose=False,
            )

            latency_ms = (time.perf_counter() - start_time) * 1000.0

            # Step 5: Extract transcript text
            text = self._extract_text(raw_output)

            return ASRResult(
                text=text.strip(),
                language=self.language,
                processing_time_ms=round(latency_ms, 2),
                audio_duration_ms=round(audio_data.duration_ms, 2),
                is_final=True,
            )
        finally:
            # Clean up temporary WAV
            if temp_wav.exists():
                try:
                    os.remove(temp_wav)
                except OSError:
                    pass

    @staticmethod
    def _extract_text(raw_output) -> str:
        """Extract transcription string from various NeMo output formats."""
        if not raw_output:
            return ""

        first = raw_output[0]
        if isinstance(first, str):
            return first
        if hasattr(first, "text"):
            return first.text
        return str(first)
