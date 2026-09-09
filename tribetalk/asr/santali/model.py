"""Santali Whisper Ol Chiki ASR model loader and device manager."""

from pathlib import Path
from typing import Optional, Union, Tuple
import logging
import torch
from transformers import WhisperProcessor, WhisperForConditionalGeneration

logger = logging.getLogger(__name__)

DEFAULT_SANTALI_MODEL_REPO: str = "thunderboltc/whisper-small-santali-ol-chiki"
BASE_WHISPER_PROCESSOR: str = "openai/whisper-small"


class SantaliASRModel:
    """Manages loading, caching, and device placement for Santali Whisper model."""

    def __init__(
        self,
        model_path_or_repo: Optional[Union[str, Path]] = None,
        device: Optional[str] = None,
    ) -> None:
        """Initialize Santali model configuration.

        Args:
            model_path_or_repo: Local model directory or Hugging Face repo ID.
                If None, uses DEFAULT_SANTALI_MODEL_REPO.
            device: 'cuda', 'cpu', or None (auto-detect).
        """
        self._model_path_or_repo = str(model_path_or_repo or DEFAULT_SANTALI_MODEL_REPO)
        if device is None:
            self._device = "cuda" if torch.cuda.is_available() else "cpu"
        else:
            self._device = device

        self._model: Optional[WhisperForConditionalGeneration] = None
        self._processor: Optional[WhisperProcessor] = None

    @property
    def is_loaded(self) -> bool:
        """Check if model and processor are currently loaded in memory."""
        return self._model is not None and self._processor is not None

    @property
    def device(self) -> str:
        """Device on which the model is loaded ('cuda' or 'cpu')."""
        return self._device

    def load(self) -> None:
        """Load the Whisper model and processor into memory.

        Idempotent: does nothing if already loaded.
        """
        if self.is_loaded:
            return

        logger.info(f"Loading Santali Whisper model from: {self._model_path_or_repo}")

        # Load processor (try model path first, fallback to base whisper-small)
        try:
            processor = WhisperProcessor.from_pretrained(self._model_path_or_repo)
        except Exception as proc_err:
            logger.info(
                f"Processor not found in {self._model_path_or_repo} ({proc_err}); "
                f"falling back to {BASE_WHISPER_PROCESSOR}"
            )
            processor = WhisperProcessor.from_pretrained(BASE_WHISPER_PROCESSOR)

        # Load Whisper model
        target_device = torch.device(self._device)
        model = WhisperForConditionalGeneration.from_pretrained(self._model_path_or_repo)
        model.eval()
        model.to(target_device)

        self._processor = processor
        self._model = model
        logger.info(f"Santali Whisper model loaded successfully on {self._device}")

    def get_model_and_processor(self) -> Tuple[WhisperForConditionalGeneration, WhisperProcessor]:
        """Return loaded model and processor, loading them if not yet loaded."""
        if not self.is_loaded:
            self.load()
        assert self._model is not None and self._processor is not None
        return self._model, self._processor

    def unload(self) -> None:
        """Unload Whisper model and processor from memory."""
        if self._model is not None:
            del self._model
            self._model = None
        if self._processor is not None:
            del self._processor
            self._processor = None
        if torch.cuda.is_available():
            torch.cuda.empty_cache()
        import gc
        gc.collect()
        logger.info("Santali Whisper ASR unloaded successfully.")

