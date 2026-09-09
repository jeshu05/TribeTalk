"""TribeTalk TTS Package: Lightweight 16 kHz Speech Synthesis for Hindi and Santali."""

from pathlib import Path
from typing import Optional, Union

from tribetalk.tts.common.types import TTSResult
from tribetalk.tts.common.interface import TTSInterface
from tribetalk.tts.hindi.engine import HindiTTS
from tribetalk.tts.santali.engine import SantaliTTS

__all__ = [
    "TTSResult",
    "TTSInterface",
    "HindiTTS",
    "SantaliTTS",
    "get_tts",
]


def get_tts(
    language: str,
    model_path_or_repo: Optional[Union[str, Path]] = None,
    lazy_load: bool = True,
    **kwargs,
) -> TTSInterface:
    """Factory function returning a configured TTSInterface instance.

    Args:
        language: Language identifier ('hi' / 'hindi' or 'sat' / 'santali').
        model_path_or_repo: Optional local model path or Hugging Face repo ID.
        lazy_load: Whether to defer model weight loading until first synthesis.
        **kwargs: Engine-specific options (e.g. device for Hindi, providers for Santali).

    Returns:
        TTSInterface instance configured for the specified language.

    Raises:
        ValueError: If language is unsupported.
    """
    lang = str(language).strip().lower()

    if lang in ("hi", "hindi", "hin"):
        return HindiTTS(
            model_path_or_repo=model_path_or_repo,
            lazy_load=lazy_load,
            **kwargs,
        )
    elif lang in ("sat", "santali", "santhali", "olchiki", "ol_chiki"):
        return SantaliTTS(
            model_path_or_repo=model_path_or_repo,
            lazy_load=lazy_load,
            **kwargs,
        )
    else:
        raise ValueError(
            f"Unsupported TTS language: {language!r}. "
            f"Supported languages are 'hi' ('hindi') and 'sat' ('santali')."
        )
