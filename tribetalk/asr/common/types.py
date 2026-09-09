"""Data structures for ASR results and audio representations."""

from dataclasses import dataclass, asdict
from typing import Optional, Dict, Any
import numpy as np


@dataclass(frozen=True)
class ASRResult:
    """Represents the output of an ASR transcription operation.

    Attributes:
        text: Transcribed text string (Hindi in Devanagari or Santali in Ol Chiki).
        language: ISO language code ("hi" for Hindi, "sat" for Santali).
        processing_time_ms: End-to-end inference latency in milliseconds.
        audio_duration_ms: Duration of the input audio in milliseconds.
        is_final: Whether this result is final (True for offline ASR).
        confidence: Optional acoustic confidence metric if genuinely provided by the model.
            Never fabricated.
    """

    text: str
    language: str
    processing_time_ms: float
    audio_duration_ms: float
    is_final: bool = True
    confidence: Optional[float] = None

    def __post_init__(self) -> None:
        """Validate result attributes."""
        if not isinstance(self.text, str):
            raise TypeError(f"text must be a string, got {type(self.text).__name__}")
        if not isinstance(self.language, str) or not self.language.strip():
            raise ValueError(f"language must be a non-empty string, got {self.language!r}")
        if self.processing_time_ms < 0:
            raise ValueError(
                f"processing_time_ms must be non-negative, got {self.processing_time_ms}"
            )
        if self.audio_duration_ms < 0:
            raise ValueError(
                f"audio_duration_ms must be non-negative, got {self.audio_duration_ms}"
            )
        if self.confidence is not None and not (0.0 <= self.confidence <= 1.0):
            raise ValueError(
                f"confidence must be between 0.0 and 1.0 if provided, got {self.confidence}"
            )

    @property
    def rtf(self) -> float:
        """Real-Time Factor (RTF) = processing_time / audio_duration.

        Values < 1.0 mean inference ran faster than real-time.
        Returns 0.0 if audio duration is zero.
        """
        if self.audio_duration_ms <= 0:
            return 0.0
        return self.processing_time_ms / self.audio_duration_ms

    def to_dict(self) -> Dict[str, Any]:
        """Convert result to dictionary representation."""
        data = asdict(self)
        data["rtf"] = round(self.rtf, 4)
        return data


@dataclass
class AudioData:
    """Preprocessed audio container standardized for downstream ASR inference.

    Attributes:
        samples: 1D numpy float32 array normalized to [-1.0, 1.0].
        sample_rate: Sampling rate in Hz (standardized to 16,000 Hz).
        duration_ms: Total duration in milliseconds.
        original_channels: Number of channels in the source audio before mono mixdown.
        original_sample_rate: Sampling rate of the source audio before resampling.
    """

    samples: np.ndarray
    sample_rate: int = 16000
    duration_ms: float = 0.0
    original_channels: int = 1
    original_sample_rate: int = 16000

    def __post_init__(self) -> None:
        if self.samples.ndim != 1:
            raise ValueError(
                f"Audio samples must be 1D (mono), got shape {self.samples.shape}"
            )
        if self.samples.dtype != np.float32:
            self.samples = self.samples.astype(np.float32)
        if self.duration_ms <= 0 and len(self.samples) > 0 and self.sample_rate > 0:
            self.duration_ms = (len(self.samples) / self.sample_rate) * 1000.0
