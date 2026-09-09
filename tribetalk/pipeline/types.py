"""Data types, enumerations, and result containers for the TribeTalk pipeline."""

from __future__ import annotations

from dataclasses import dataclass
from enum import Enum
from typing import Any, Dict, Optional, Union

from tribetalk.asr.common.types import ASRResult
from tribetalk.translation.common.types import TranslationResult
from tribetalk.tts.common.types import TTSResult


class PipelineState(str, Enum):
    """Lifecycle states of the TribeTalk translation pipeline."""
    IDLE = "IDLE"
    LISTENING = "LISTENING"
    ASR_PROCESSING = "ASR_PROCESSING"
    TRANSLATING = "TRANSLATING"
    SYNTHESIZING = "SYNTHESIZING"
    PLAYBACK = "PLAYBACK"
    COMPLETED = "COMPLETED"
    ERROR = "ERROR"


class PipelineDirection(str, Enum):
    """Translation directions supported by TribeTalk."""
    HINDI_TO_SANTALI = "hi_to_sat"
    SANTALI_TO_HINDI = "sat_to_hi"

    @classmethod
    def from_string(cls, value: Union[str, PipelineDirection]) -> PipelineDirection:
        """Parse direction from string aliases."""
        if isinstance(value, cls):
            return value

        val = value.strip().lower().replace(" ", "").replace("->", "_to_").replace("-", "_")
        if val in ("hi_to_sat", "hindi_to_santali", "hin_to_sat", "hi_sat"):
            return cls.HINDI_TO_SANTALI
        elif val in ("sat_to_hi", "santali_to_hindi", "sat_to_hin", "sat_hi"):
            return cls.SANTALI_TO_HINDI
        else:
            raise ValueError(
                f"Unsupported pipeline direction '{value}'. Supported: 'hi_to_sat', 'sat_to_hi'"
            )

    @property
    def source_lang(self) -> str:
        return "hi" if self == PipelineDirection.HINDI_TO_SANTALI else "sat"

    @property
    def target_lang(self) -> str:
        return "sat" if self == PipelineDirection.HINDI_TO_SANTALI else "hi"


@dataclass(frozen=True)
class PipelineResult:
    """Immutable result container representing a completed or failed pipeline turn."""
    direction: PipelineDirection
    source_language: str
    target_language: str
    source_text: str = ""
    target_text: str = ""
    asr_result: Optional[ASRResult] = None
    translation_result: Optional[TranslationResult] = None
    tts_result: Optional[TTSResult] = None
    total_latency_ms: float = 0.0
    success: bool = True
    error_message: Optional[str] = None

    def __post_init__(self) -> None:
        if self.total_latency_ms < 0:
            raise ValueError(f"total_latency_ms cannot be negative, got {self.total_latency_ms}")

    @property
    def has_audio_output(self) -> bool:
        """Check if speech synthesis generated valid audio."""
        return self.tts_result is not None and len(self.tts_result.audio) > 0

    def to_dict(self) -> Dict[str, Any]:
        """Serialize result to dictionary."""
        res: Dict[str, Any] = {
            "direction": self.direction.value,
            "source_language": self.source_language,
            "target_language": self.target_language,
            "source_text": self.source_text,
            "target_text": self.target_text,
            "total_latency_ms": self.total_latency_ms,
            "success": self.success,
            "error_message": self.error_message,
            "has_audio_output": self.has_audio_output,
        }
        if self.asr_result:
            res["asr_result"] = self.asr_result.to_dict()
        if self.translation_result:
            res["translation_result"] = self.translation_result.to_dict()
        if self.tts_result:
            res["tts_result"] = self.tts_result.to_dict()
        return res
