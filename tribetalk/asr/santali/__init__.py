"""Santali speech recognition package using Whisper fine-tuned for Ol Chiki."""

from tribetalk.asr.santali.model import SantaliASRModel
from tribetalk.asr.santali.inference import SantaliASR

__all__ = ["SantaliASRModel", "SantaliASR"]
