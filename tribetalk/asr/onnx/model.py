"""ONNX Runtime model loader and session manager for IndicConformer ASR.

Supports both Hindi (Devanagari) and Santali (Ol Chiki) using identical
INT8 quantized CTC graphs with zero PyTorch or NeMo dependency.
"""

from __future__ import annotations

import gc
import logging
from pathlib import Path
from typing import Dict, Optional, Tuple, Union

from huggingface_hub import hf_hub_download
import onnxruntime as ort

logger = logging.getLogger(__name__)

HINDI_ONNX_REPO: str = "OpenVoiceOS/ai4bharat-indicconformer-hi-onnx"
SANTALI_ONNX_REPO: str = "OpenVoiceOS/ai4bharat-indicconformer-sat-onnx"
DEFAULT_ONNX_MODEL_FILE: str = "model.int8.onnx"
DEFAULT_VOCAB_FILE: str = "vocab.txt"


class IndicConformerONNXModel:
    """Manages loading, vocabulary mapping, and inference sessions for IndicConformer ONNX."""

    def __init__(
        self,
        language: str,
        model_path_or_repo: Optional[Union[str, Path]] = None,
        use_int8: bool = True,
    ) -> None:
        """Initialize configuration for Hindi or Santali ONNX ASR.

        Args:
            language: 'hi' or 'sat'.
            model_path_or_repo: Optional local directory or Hugging Face repo ID.
            use_int8: If True, uses model.int8.onnx (131.3 MB); otherwise model.onnx.
        """
        lang = language.lower().strip()
        if lang in ("hi", "hindi", "hin"):
            self.language = "hi"
            default_repo = HINDI_ONNX_REPO
        elif lang in ("sat", "santali", "olchiki", "santhali"):
            self.language = "sat"
            default_repo = SANTALI_ONNX_REPO
        else:
            raise ValueError(f"Unsupported language '{language}' for IndicConformer ONNX. Expected 'hi' or 'sat'.")

        self._model_path_or_repo = str(model_path_or_repo or default_repo)
        self._model_filename = DEFAULT_ONNX_MODEL_FILE if use_int8 else "model.onnx"

        self._session: Optional[ort.InferenceSession] = None
        self._vocab: Optional[Dict[int, str]] = None
        self._blank_id: int = 256

    @property
    def is_loaded(self) -> bool:
        """Whether the ONNX session and vocabulary are resident in memory."""
        return self._session is not None and self._vocab is not None

    def load(self) -> None:
        """Download (if needed) and load the ONNX session and vocabulary.

        Idempotent: skips if already loaded.
        """
        if self.is_loaded:
            return

        logger.info(
            "Loading IndicConformer ONNX for language '%s' from: %s (%s)",
            self.language,
            self._model_path_or_repo,
            self._model_filename,
        )

        candidate = Path(self._model_path_or_repo)
        if candidate.is_file():
            model_path = candidate
            vocab_path = candidate.parent / DEFAULT_VOCAB_FILE
        elif candidate.is_dir():
            model_path = candidate / self._model_filename
            vocab_path = candidate / DEFAULT_VOCAB_FILE
        else:
            model_path = Path(
                hf_hub_download(
                    repo_id=self._model_path_or_repo,
                    filename=self._model_filename,
                )
            )
            vocab_path = Path(
                hf_hub_download(
                    repo_id=self._model_path_or_repo,
                    filename=DEFAULT_VOCAB_FILE,
                )
            )

        # 1. Parse vocab.txt
        vocab: Dict[int, str] = {}
        with open(vocab_path, "r", encoding="utf-8") as f:
            for line in f:
                line = line.strip()
                if not line:
                    continue
                parts = line.split()
                if len(parts) >= 2:
                    piece = parts[0]
                    idx = int(parts[1])
                    vocab[idx] = piece
                    if piece == "<blk>":
                        self._blank_id = idx

        self._vocab = vocab

        # 2. Configure ONNX session
        opts = ort.SessionOptions()
        opts.graph_optimization_level = ort.GraphOptimizationLevel.ORT_ENABLE_ALL
        opts.intra_op_num_threads = 4

        self._session = ort.InferenceSession(
            str(model_path),
            sess_options=opts,
            providers=["CPUExecutionProvider"],
        )
        logger.info("IndicConformer ONNX (%s) session loaded successfully.", self.language)

    def unload(self) -> None:
        """Unload ONNX session and vocabulary to release memory."""
        self._session = None
        self._vocab = None
        gc.collect()
        logger.info("IndicConformer ONNX (%s) unloaded and memory reclaimed.", self.language)

    def get_session_and_vocab(self) -> Tuple[ort.InferenceSession, Dict[int, str], int]:
        """Return active ONNX session, vocabulary mapping, and blank token ID."""
        if not self.is_loaded:
            self.load()
        assert self._session is not None and self._vocab is not None
        return self._session, self._vocab, self._blank_id
