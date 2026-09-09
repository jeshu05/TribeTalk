"""Santali Piper VITS ONNX model loader and session manager."""

from pathlib import Path
from typing import Optional, Union, Tuple, Any, Dict, List
import json
import gc
import logging
import numpy as np

logger = logging.getLogger(__name__)

DEFAULT_SANTALI_TTS_REPO: str = "Ashraf01k/vernacular-pedagogy-santhali"
DEFAULT_MODEL_FILE: str = "sat_piper_model.onnx"
DEFAULT_CONFIG_FILE: str = "sat_piper_model.onnx.json"


class SantaliTTSModel:
    """Manages loading, session lifecycle, and symbol vocabulary for Santali Piper ONNX."""

    def __init__(
        self,
        model_path_or_repo: Optional[Union[str, Path]] = None,
        providers: Optional[List[str]] = None,
    ) -> None:
        """Initialize Santali TTS configuration.

        Args:
            model_path_or_repo: Local directory containing .onnx and .onnx.json, or HF repo ID.
            providers: ONNX Runtime execution providers (default: ['CPUExecutionProvider']).
        """
        self._model_path_or_repo = str(model_path_or_repo or DEFAULT_SANTALI_TTS_REPO)
        self._providers = providers or ["CPUExecutionProvider"]

        self._session: Optional[Any] = None
        self._config: Optional[Dict[str, Any]] = None
        self._phoneme_id_map: Optional[Dict[str, List[int]]] = None
        self._scales: Optional[np.ndarray] = None
        self._sample_rate: int = 16000

    @property
    def is_loaded(self) -> bool:
        """Whether ONNX session and vocabulary are resident in memory."""
        return self._session is not None and self._phoneme_id_map is not None

    @property
    def sample_rate(self) -> int:
        """Audio sample rate in Hz."""
        return self._sample_rate

    def load(self) -> None:
        """Load the ONNX session and configuration into memory.

        Idempotent: skips if already loaded.
        """
        if self.is_loaded:
            return

        import onnxruntime as ort

        model_path, config_path = self._resolve_model_files()
        logger.info("Loading Santali Piper ONNX from: %s", model_path)

        # 1. Load config and vocabulary map
        with open(config_path, "r", encoding="utf-8") as f:
            cfg = json.load(f)

        self._config = cfg
        self._phoneme_id_map = cfg.get("phoneme_id_map", {})
        self._sample_rate = cfg.get("audio", {}).get("sample_rate", 16000)

        # 2. Setup inference scales: [noise_scale, length_scale, noise_w]
        inf_cfg = cfg.get("inference", {})
        self._scales = np.array(
            [
                inf_cfg.get("noise_scale", 0.667),
                inf_cfg.get("length_scale", 1.0),
                inf_cfg.get("noise_w", 0.8),
            ],
            dtype=np.float32,
        )

        # 3. Setup ONNX session
        sess_opts = ort.SessionOptions()
        sess_opts.graph_optimization_level = ort.GraphOptimizationLevel.ORT_ENABLE_ALL
        self._session = ort.InferenceSession(
            str(model_path),
            sess_options=sess_opts,
            providers=self._providers,
        )

        logger.info(
            "Santali Piper ONNX loaded successfully (sample_rate=%d, symbols=%d)",
            self._sample_rate,
            len(self._phoneme_id_map),
        )

    def unload(self) -> None:
        """Unload ONNX session and release memory."""
        self._session = None
        self._config = None
        self._phoneme_id_map = None
        self._scales = None
        gc.collect()
        logger.info("Santali Piper ONNX unloaded and memory reclaimed.")

    def _resolve_model_files(self) -> Tuple[Path, Path]:
        """Locate ONNX and JSON files on local disk or download from Hugging Face."""
        candidate = Path(self._model_path_or_repo)
        if candidate.is_dir():
            onnx_path = candidate / DEFAULT_MODEL_FILE
            json_path = candidate / DEFAULT_CONFIG_FILE
            if onnx_path.exists() and json_path.exists():
                return onnx_path, json_path

        # Download from Hugging Face hub
        from huggingface_hub import hf_hub_download

        onnx_file = hf_hub_download(
            repo_id=self._model_path_or_repo,
            filename=DEFAULT_MODEL_FILE,
        )
        json_file = hf_hub_download(
            repo_id=self._model_path_or_repo,
            filename=DEFAULT_CONFIG_FILE,
        )
        return Path(onnx_file), Path(json_file)

    def get_session_and_config(self) -> Tuple[Any, Dict[str, List[int]], np.ndarray, int]:
        """Return loaded session and tokenization components."""
        if not self.is_loaded:
            self.load()
        assert (
            self._session is not None
            and self._phoneme_id_map is not None
            and self._scales is not None
        )
        return self._session, self._phoneme_id_map, self._scales, self._sample_rate
