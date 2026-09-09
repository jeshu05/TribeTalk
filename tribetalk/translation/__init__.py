"""TribeTalk Translation Package: Bidirectional Hindi <-> Santali Neural Translation."""

from pathlib import Path
from typing import Optional, Union, List

from tribetalk.translation.common.types import TranslationResult, LanguageTag
from tribetalk.translation.common.interface import TranslationInterface
from tribetalk.translation.normalization import TextNormalizer
from tribetalk.translation.indictrans.engine import IndicTransEngine

__all__ = [
    "TranslationResult",
    "LanguageTag",
    "TranslationInterface",
    "TextNormalizer",
    "IndicTransEngine",
    "get_translator",
]


def get_translator(
    model_path_or_repo: Optional[Union[str, Path]] = None,
    providers: Optional[List[str]] = None,
    lazy_load: bool = True,
    **kwargs,
) -> TranslationInterface:
    """Factory function returning a configured TranslationInterface instance.

    Args:
        model_path_or_repo: Path to local ONNX directory or Hugging Face repo ID.
        providers: Execution providers (e.g. ['CPUExecutionProvider']).
        lazy_load: Whether to defer session loading until first translation.
        **kwargs: Additional parameters passed to engine.

    Returns:
        TranslationInterface instance ready for bidirectional Hindi <-> Santali translation.
    """
    return IndicTransEngine(
        model_path_or_repo=model_path_or_repo,
        providers=providers,
        lazy_load=lazy_load,
        **kwargs,
    )
