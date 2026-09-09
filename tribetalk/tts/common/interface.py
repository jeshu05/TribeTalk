"""Abstract base class defining the unified TribeTalk Text-to-Speech (TTS) interface."""

from abc import ABC, abstractmethod
from pathlib import Path
from typing import Union
from tribetalk.tts.common.types import TTSResult


class TTSInterface(ABC):
    """Unified interface for TribeTalk speech synthesis engines.

    Every TTS engine (Hindi Meta MMS VITS, Santali Piper ONNX) must implement
    this contract to allow seamless orchestration and dynamic memory management.
    """

    @property
    @abstractmethod
    def language(self) -> str:
        """Language code handled by this TTS engine ('hi' or 'sat')."""
        pass

    @property
    @abstractmethod
    def sample_rate(self) -> int:
        """Native audio sample rate in Hz (standardized to 16,000 Hz)."""
        pass

    @property
    @abstractmethod
    def is_loaded(self) -> bool:
        """Whether model weights and sessions are resident in memory."""
        pass

    @abstractmethod
    def load_model(self) -> None:
        """Load model weights/sessions into memory.

        Must be idempotent: repeated calls should not duplicate memory allocations.
        """
        pass

    @abstractmethod
    def unload_model(self) -> None:
        """Unload model weights/sessions and release memory back to the host OS.

        Must be idempotent: safe to call when already unloaded.
        """
        pass

    @abstractmethod
    def synthesize(self, text: str) -> TTSResult:
        """Synthesize text into a natural spoken waveform.

        Args:
            text: Input sentence in the appropriate script (Devanagari or Ol Chiki).

        Returns:
            TTSResult containing the 16 kHz 1D audio array, timing, and metadata.

        Raises:
            ValueError: If text is empty or contains unsupported characters.
            RuntimeError: If synthesis inference fails.
        """
        pass

    def save_to_wav(self, text: str, output_path: Union[str, Path]) -> Path:
        """Synthesize text and save the resulting audio directly to a WAV file.

        Args:
            text: Input text sentence.
            output_path: Destination path for the WAV file.

        Returns:
            Path to the saved WAV file.
        """
        result = self.synthesize(text)
        return result.save_wav(output_path)
