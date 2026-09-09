"""TribeTalk ASR package: Offline-capable speech recognition for Hindi and Santali."""

from typing import Union, Optional
from pathlib import Path

from tribetalk.asr.common.types import ASRResult, AudioData
from tribetalk.asr.common.interface import ASRInterface
from tribetalk.asr.common.audio import (
    TARGET_SAMPLE_RATE,
    AudioLoadingError,
    load_and_preprocess_audio,
    save_temp_wav,
)
from tribetalk.asr.hindi.inference import HindiASR
from tribetalk.asr.santali.inference import SantaliASR
from tribetalk.asr.onnx.engine import IndicConformerONNX

__all__ = [
    "ASRResult",
    "AudioData",
    "ASRInterface",
    "HindiASR",
    "SantaliASR",
    "IndicConformerONNX",
    "TARGET_SAMPLE_RATE",
    "AudioLoadingError",
    "load_and_preprocess_audio",
    "save_temp_wav",
    "get_asr",
]


def get_asr(
    language: str,
    model_path_or_repo: Optional[Union[str, Path]] = None,
    device: Optional[str] = None,
    lazy_load: bool = True,
    backend: str = "desktop",
    **kwargs,
) -> ASRInterface:
    """Factory function to instantiate an ASR engine by language and backend.

    Args:
        language: Language identifier ('hi' / 'hindi' or 'sat' / 'santali').
        model_path_or_repo: Optional local model path or Hugging Face repo ID.
        device: 'cuda', 'cpu', or None (auto-detect).
        lazy_load: Whether to defer model weight loading until first transcription.
        backend: 'desktop' (reference NeMo / Whisper) or 'onnx' (production INT8 IndicConformer).
        **kwargs: Engine-specific options.

    Returns:
        ASRInterface instance configured for the specified language.

    Raises:
        ValueError: If language or backend is unsupported.
    """
    lang = language.strip().lower()

    if backend.lower() == "onnx":
        if lang in ("hi", "hindi", "hin", "sat", "santali", "olchiki"):
            return IndicConformerONNX(
                language=lang,
                model_path_or_repo=model_path_or_repo,
                lazy_load=lazy_load,
                **kwargs,
            )
        else:
            raise ValueError(f"Unsupported ASR language for ONNX backend: {language!r}")

    # Desktop reference backend
    if lang in ("hi", "hindi"):
        return HindiASR(
            model_path_or_repo=model_path_or_repo,
            device=device,
            lazy_load=lazy_load,
            **kwargs,
        )
    elif lang in ("sat", "santali"):
        return SantaliASR(
            model_path_or_repo=model_path_or_repo,
            device=device,
            lazy_load=lazy_load,
            **kwargs,
        )
    else:
        raise ValueError(
            f"Unsupported ASR language: {language!r}. "
            f"Supported languages are 'hi' ('hindi') and 'sat' ('santali')."
        )
