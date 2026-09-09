"""Data types and language code mappings for the TribeTalk translation subsystem."""

from dataclasses import dataclass, asdict
from typing import Dict, Any


class LanguageTag:
    """Language code normalization and mapping utility for Hindi and Santali."""

    HINDI_CANONICAL = "hin_Deva"
    SANTALI_CANONICAL = "sat_Olck"

    HINDI_SHORT = "hi"
    SANTALI_SHORT = "sat"

    _ALIASES: Dict[str, str] = {
        # Hindi aliases
        "hi": HINDI_CANONICAL,
        "hin": HINDI_CANONICAL,
        "hindi": HINDI_CANONICAL,
        "hin_deva": HINDI_CANONICAL,
        # Santali aliases
        "sat": SANTALI_CANONICAL,
        "santali": SANTALI_CANONICAL,
        "santhali": SANTALI_CANONICAL,
        "olchiki": SANTALI_CANONICAL,
        "ol_chiki": SANTALI_CANONICAL,
        "sat_olck": SANTALI_CANONICAL,
    }

    _SHORT_CODES: Dict[str, str] = {
        HINDI_CANONICAL: HINDI_SHORT,
        SANTALI_CANONICAL: SANTALI_SHORT,
        HINDI_SHORT: HINDI_SHORT,
        SANTALI_SHORT: SANTALI_SHORT,
    }

    @classmethod
    def to_canonical(cls, lang: str) -> str:
        """Convert a language identifier or alias to canonical FLORES tag.

        Args:
            lang: Language string (e.g. 'hi', 'hindi', 'sat', 'santali').

        Returns:
            'hin_Deva' or 'sat_Olck'.

        Raises:
            ValueError: If the language is unsupported.
        """
        cleaned = str(lang).strip().lower()
        if cleaned in cls._ALIASES:
            return cls._ALIASES[cleaned]
        # Check direct case-insensitive match against canonical forms
        if cleaned == cls.HINDI_CANONICAL.lower():
            return cls.HINDI_CANONICAL
        if cleaned == cls.SANTALI_CANONICAL.lower():
            return cls.SANTALI_CANONICAL

        raise ValueError(
            f"Unsupported translation language: {lang!r}. "
            f"Supported languages are Hindi ('hi') and Santali ('sat')."
        )

    @classmethod
    def to_short(cls, lang: str) -> str:
        """Convert any supported language tag or alias to short code ('hi' or 'sat')."""
        canonical = cls.to_canonical(lang)
        return cls._SHORT_CODES[canonical]

    @classmethod
    def is_supported(cls, lang: str) -> bool:
        """Check whether a language code or alias is supported."""
        try:
            cls.to_canonical(lang)
            return True
        except ValueError:
            return False


@dataclass(frozen=True)
class TranslationResult:
    """Immutable result of a text translation operation.

    Attributes:
        source_text: Input source sentence.
        target_text: Translated target sentence in the appropriate script.
        source_language: Source language code ('hi' or 'sat').
        target_language: Target language code ('sat' or 'hi').
        processing_time_ms: Wall-clock latency of translation in milliseconds.
        model_name: Name or identifier of the translation model.
        is_final: Whether this translation is final (default True).
    """

    source_text: str
    target_text: str
    source_language: str
    target_language: str
    processing_time_ms: float
    model_name: str = "IndicTrans2-320M-ONNX-INT8"
    is_final: bool = True

    def __post_init__(self) -> None:
        """Validate result attributes."""
        if not isinstance(self.source_text, str):
            raise TypeError(
                f"source_text must be str, got {type(self.source_text).__name__}"
            )
        if not isinstance(self.target_text, str):
            raise TypeError(
                f"target_text must be str, got {type(self.target_text).__name__}"
            )
        if not isinstance(self.source_language, str) or not self.source_language.strip():
            raise ValueError(
                f"source_language must be non-empty string, got {self.source_language!r}"
            )
        if not isinstance(self.target_language, str) or not self.target_language.strip():
            raise ValueError(
                f"target_language must be non-empty string, got {self.target_language!r}"
            )
        if self.processing_time_ms < 0:
            raise ValueError(
                f"processing_time_ms must be non-negative, got {self.processing_time_ms}"
            )

    def to_dict(self) -> Dict[str, Any]:
        """Convert translation result to a serializable dictionary."""
        return asdict(self)
