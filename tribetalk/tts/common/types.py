"""Data structures for Text-to-Speech (TTS) synthesis results."""

from dataclasses import dataclass
from pathlib import Path
from typing import Union, Dict, Any
import numpy as np
import soundfile as sf


@dataclass(frozen=True)
class TTSResult:
    """Represents the output of a TTS speech synthesis operation.

    Attributes:
        audio: 1D numpy float32 array normalized to [-1.0, 1.0].
        sample_rate: Sampling rate of the audio in Hz (standardized to 16,000 Hz).
        text: Input text string that was synthesized.
        language: Language code ('hi' or 'sat').
        duration_ms: Duration of the generated audio in milliseconds.
        processing_time_ms: Synthesis latency in milliseconds.
        model_name: Identifier of the TTS model used.
        is_final: Whether this synthesized result is final.
    """

    audio: np.ndarray
    sample_rate: int = 16000
    text: str = ""
    language: str = "hi"
    duration_ms: float = 0.0
    processing_time_ms: float = 0.0
    model_name: str = "VITS"
    is_final: bool = True

    def __post_init__(self) -> None:
        """Validate result attributes."""
        if not isinstance(self.audio, np.ndarray):
            raise TypeError(f"audio must be a numpy ndarray, got {type(self.audio).__name__}")
        if self.audio.ndim != 1:
            raise ValueError(f"audio must be 1D (mono), got shape {self.audio.shape}")
        if self.audio.dtype != np.float32:
            # Overwrite frozen field safely
            object.__setattr__(self, "audio", self.audio.astype(np.float32))

        if self.sample_rate <= 0:
            raise ValueError(f"sample_rate must be positive, got {self.sample_rate}")
        if not isinstance(self.language, str) or not self.language.strip():
            raise ValueError(f"language must be a non-empty string, got {self.language!r}")
        if self.processing_time_ms < 0:
            raise ValueError(
                f"processing_time_ms must be non-negative, got {self.processing_time_ms}"
            )

        if self.duration_ms <= 0.0 and len(self.audio) > 0 and self.sample_rate > 0:
            calc_duration = (len(self.audio) / self.sample_rate) * 1000.0
            object.__setattr__(self, "duration_ms", round(calc_duration, 2))

    @property
    def rtf(self) -> float:
        """Real-Time Factor (RTF) = processing_time / duration.

        Values < 1.0 indicate faster-than-real-time synthesis.
        """
        if self.duration_ms <= 0:
            return 0.0
        return self.processing_time_ms / self.duration_ms

    def save_wav(self, output_path: Union[str, Path]) -> Path:
        """Save synthesized audio to a 16 kHz mono WAV file.

        Args:
            output_path: Destination path for the WAV file.

        Returns:
            Path to the saved WAV file.
        """
        dest = Path(output_path).resolve()
        dest.parent.mkdir(parents=True, exist_ok=True)
        sf.write(str(dest), self.audio, self.sample_rate, subtype="PCM_16")
        return dest

    def to_dict(self) -> Dict[str, Any]:
        """Convert TTS result to a serializable dictionary (omits raw audio array)."""
        return {
            "text": self.text,
            "language": self.language,
            "sample_rate": self.sample_rate,
            "duration_ms": round(self.duration_ms, 2),
            "processing_time_ms": round(self.processing_time_ms, 2),
            "rtf": round(self.rtf, 4),
            "audio_samples_count": len(self.audio),
            "model_name": self.model_name,
            "is_final": self.is_final,
        }
