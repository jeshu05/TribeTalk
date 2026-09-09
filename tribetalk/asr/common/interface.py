"""Abstract base class defining the unified TribeTalk ASR interface."""

from abc import ABC, abstractmethod
from pathlib import Path
from typing import Union
from tribetalk.asr.common.types import ASRResult


class ASRInterface(ABC):
    """Unified interface for TribeTalk speech recognition engines.

    Every ASR engine (Hindi IndicConformer, Santali Whisper, etc.) must implement
    this contract to allow seamless routing and consistent higher-level orchestration.
    """

    @property
    @abstractmethod
    def language(self) -> str:
        """Target language code for this recognizer ('hi' or 'sat')."""
        pass

    @property
    @abstractmethod
    def is_loaded(self) -> bool:
        """Whether model weights and processors are currently loaded in memory."""
        pass

    @abstractmethod
    def load_model(self) -> None:
        """Load model weights and processor onto the configured device.

        Must be idempotent: repeated calls should not reload the model if already loaded.
        """
        pass

    @abstractmethod
    def transcribe(self, audio_path: Union[str, Path]) -> ASRResult:
        """Transcribe an audio file to text.

        Args:
            audio_path: Path to the audio file (WAV, FLAC, MP3, etc.).

        Returns:
            ASRResult containing transcribed text, language, timing, and metadata.

        Raises:
            FileNotFoundError: If audio_path does not exist.
            AudioLoadingError: If the audio file is corrupted or unreadable.
            RuntimeError: If model inference fails.
        """
        pass

    def unload_model(self) -> None:
        """Unload model weights and processors to release memory.

        Subclasses may override to free native accelerator / CPU buffers.
        """
        pass

