"""Hindi TTS model loader and ONNX Runtime session manager for Meta MMS VITS.

Replaces PyTorch and Transformers with pure ONNX Runtime, eliminating
runtime fragmentation and heavy framework memory overhead.
"""

from __future__ import annotations

import gc
import json
import logging
from pathlib import Path
from typing import Dict, Optional, Tuple, Union

from huggingface_hub import hf_hub_download
import onnxruntime as ort

logger = logging.getLogger(__name__)

DEFAULT_HINDI_TTS_REPO: str = "Xenova/mms-tts-hin"
DEFAULT_MODEL_FILE: str = "onnx/model.onnx"
DEFAULT_VOCAB_FILE: str = "vocab.json"


class HindiTTSModel:
    """Manages downloading, caching, and ONNX Runtime execution for Hindi MMS VITS."""

    def __init__(
        self,
        model_path_or_repo: Optional[Union[str, Path]] = None,
        use_quantized: bool = False,
    ) -> None:
        """Initialize model configuration.

        Args:
            model_path_or_repo: Local directory or Hugging Face repo ID.
            use_quantized: If True, uses the INT8 quantized model (onnx/model_quantized.onnx).
        """
        self._model_path_or_repo = str(model_path_or_repo or DEFAULT_HINDI_TTS_REPO)
        self._use_quantized = use_quantized
        self._model_filename = (
            "onnx/model_quantized.onnx" if use_quantized else DEFAULT_MODEL_FILE
        )

        self._session: Optional[ort.InferenceSession] = None
        self._vocab: Optional[Dict[str, int]] = None
        self._model_path: Optional[Path] = None

    @property
    def is_loaded(self) -> bool:
        """Whether the ONNX session and vocabulary are resident in memory."""
        return self._session is not None and self._vocab is not None

    def load(self) -> None:
        """Download (if needed) and initialize the ONNX Runtime session and vocabulary.

        Idempotent: skips if already loaded.
        """
        if self.is_loaded:
            return

        logger.info(
            "Loading Hindi TTS ONNX from: %s (%s)",
            self._model_path_or_repo,
            self._model_filename,
        )

        # 1. Resolve model path
        candidate = Path(self._model_path_or_repo)
        if candidate.is_file():
            model_path = candidate
            vocab_path = candidate.parent / DEFAULT_VOCAB_FILE
        elif candidate.is_dir():
            model_path = candidate / self._model_filename
            vocab_path = candidate / DEFAULT_VOCAB_FILE
        else:
            # Download from Hugging Face
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

        self._model_path = model_path

        # 2. Load vocabulary
        with open(vocab_path, "r", encoding="utf-8") as f:
            self._vocab = json.load(f)

        # 3. Configure ONNX Runtime session
        opts = ort.SessionOptions()
        opts.graph_optimization_level = ort.GraphOptimizationLevel.ORT_ENABLE_ALL
        opts.intra_op_num_threads = 4

        self._session = ort.InferenceSession(
            str(model_path),
            sess_options=opts,
            providers=["CPUExecutionProvider"],
        )
        logger.info("Hindi TTS ONNX session initialized successfully.")

    def unload(self) -> None:
        """Unload ONNX session and clear vocabulary from memory."""
        self._session = None
        self._vocab = None
        gc.collect()
        logger.info("Hindi TTS ONNX session unloaded and memory reclaimed.")

    def get_session_and_vocab(self) -> Tuple[ort.InferenceSession, Dict[str, int]]:
        """Return active ONNX session and vocabulary, loading them if needed."""
        if not self.is_loaded:
            self.load()
        assert self._session is not None and self._vocab is not None
        return self._session, self._vocab
