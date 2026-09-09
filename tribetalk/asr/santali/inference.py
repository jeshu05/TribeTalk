"""Santali ASR inference engine producing Ol Chiki text."""

from pathlib import Path
from typing import Optional, Union
import time
import logging
import torch

from tribetalk.asr.common.interface import ASRInterface
from tribetalk.asr.common.types import ASRResult
from tribetalk.asr.common.audio import load_and_preprocess_audio
from tribetalk.asr.santali.model import SantaliASRModel

logger = logging.getLogger(__name__)


class SantaliASR(ASRInterface):
    """Santali speech recognizer using fine-tuned Whisper (outputs Ol Chiki script)."""

    def __init__(
        self,
        model_path_or_repo: Optional[Union[str, Path]] = None,
        device: Optional[str] = None,
        lazy_load: bool = True,
    ) -> None:
        """Initialize Santali ASR engine.

        Args:
            model_path_or_repo: Local model directory or Hugging Face repo ID.
            device: 'cuda', 'cpu', or None (auto-detect).
            lazy_load: If False, loads model immediately upon initialization.
        """
        self._model_mgr = SantaliASRModel(
            model_path_or_repo=model_path_or_repo,
            device=device,
        )
        if not lazy_load:
            self._model_mgr.load()

    @property
    def language(self) -> str:
        """Target language code ('sat')."""
        return "sat"

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
        """Transcribe Santali speech from an audio file into Ol Chiki text.

        Args:
            audio_path: Path to audio file.

        Returns:
            ASRResult containing Santali Ol Chiki transcript and metrics.
        """
        # Step 1: Preprocess audio using shared pipeline (guaranteed 16 kHz mono float32)
        audio_data = load_and_preprocess_audio(audio_path)

        # Step 2: Ensure model and processor are loaded
        model, processor = self._model_mgr.get_model_and_processor()
        device = torch.device(self._model_mgr.device)

        start_time = time.perf_counter()

        # Step 3: Extract log-mel spectrogram features
        inputs = processor(
            audio_data.samples,
            sampling_rate=audio_data.sample_rate,
            return_tensors="pt",
        )
        input_features = inputs.input_features.to(device)

        # Step 4: Generate tokens with Whisper model
        with torch.no_grad():
            predicted_ids = model.generate(
                input_features=input_features,
                task="transcribe",
                max_new_tokens=440,
            )

        # Step 5: Decode token IDs to string (Ol Chiki)
        transcription = processor.batch_decode(
            predicted_ids,
            skip_special_tokens=True,
        )[0].strip()

        latency_ms = (time.perf_counter() - start_time) * 1000.0

        return ASRResult(
            text=transcription,
            language=self.language,
            processing_time_ms=round(latency_ms, 2),
            audio_duration_ms=round(audio_data.duration_ms, 2),
            is_final=True,
        )
