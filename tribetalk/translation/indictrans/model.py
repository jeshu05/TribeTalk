"""IndicTrans2 INT8 ONNX model loader, session manager, and lifecycle governor."""

from pathlib import Path
from typing import Optional, Union, List, Tuple, Any, Dict
import os
import gc
import json
import logging

logger = logging.getLogger(__name__)

DEFAULT_TRANSLATION_REPO: str = "hari31416/indictrans2-indic-indic-dist-320M-ONNX-int8"


def _ensure_indic_processor_compatibility() -> None:
    """Ensure IndicTransToolkit imports correctly with modern transformers releases."""
    try:
        import transformers.tokenization_utils as tu
        import transformers.tokenization_utils_base as tub
        if not hasattr(tu, "PreTrainedTokenizerBase"):
            tu.PreTrainedTokenizerBase = tub.PreTrainedTokenizerBase
    except Exception as e:
        logger.debug("tokenization_utils bridge notice: %s", e)


class IndicTransModelManager:
    """Manages downloading, ONNX session initialization, and memory deallocation for IndicTrans2."""

    def __init__(
        self,
        model_path_or_repo: Optional[Union[str, Path]] = None,
        providers: Optional[List[str]] = None,
    ) -> None:
        """Initialize model configuration.

        Args:
            model_path_or_repo: Local directory containing ONNX files, or Hugging Face repo ID.
            providers: ONNX Runtime execution providers (default: ['CPUExecutionProvider']).
        """
        self._model_path_or_repo = str(model_path_or_repo or DEFAULT_TRANSLATION_REPO)
        self._providers = providers or ["CPUExecutionProvider"]

        # Loaded state
        self._local_snapshot_dir: Optional[Path] = None
        self._enc_session: Optional[Any] = None
        self._dec_session: Optional[Any] = None
        self._dec_past_session: Optional[Any] = None
        self._src_tokenizer: Optional[Any] = None
        self._tgt_tokenizer: Optional[Any] = None
        self._indic_processor: Optional[Any] = None
        self._meta: Optional[Dict[str, Any]] = None
        self._decoder_start_id: int = 2
        self._eos_id: int = 2
        self._num_layers: int = 6

    @property
    def is_loaded(self) -> bool:
        """Whether ONNX sessions and tokenizers are resident in memory."""
        return (
            self._enc_session is not None
            and self._dec_session is not None
            and self._dec_past_session is not None
            and self._indic_processor is not None
        )

    @property
    def providers(self) -> List[str]:
        """Configured ONNX execution providers."""
        return self._providers

    def load(self) -> None:
        """Load ONNX sessions, tokenizers, and processor into memory.

        Idempotent: skips loading if already resident.
        """
        if self.is_loaded:
            return

        import onnxruntime as ort
        from tokenizers import Tokenizer

        _ensure_indic_processor_compatibility()
        from IndicTransToolkit import IndicProcessor

        snap = self._resolve_model_dir()
        logger.info("Loading IndicTrans2 ONNX sessions from: %s", snap)

        # 1. Initialize processor
        self._indic_processor = IndicProcessor(inference=True)

        # 2. Load tokenizers
        src_tok_path = snap / "tokenizer_src.json"
        tgt_tok_path = snap / "tokenizer_tgt.json"
        meta_path = snap / "tokenizer_meta.json"

        if not src_tok_path.exists() or not tgt_tok_path.exists():
            raise FileNotFoundError(
                f"Missing tokenizer files in {snap}. Expected tokenizer_src.json and tokenizer_tgt.json."
            )

        self._src_tokenizer = Tokenizer.from_file(str(src_tok_path))
        self._tgt_tokenizer = Tokenizer.from_file(str(tgt_tok_path))
        if meta_path.exists():
            self._meta = json.loads(meta_path.read_text(encoding="utf-8"))

        # 3. Load generation config
        gen_cfg_path = snap / "generation_config.json"
        if gen_cfg_path.exists():
            gen_cfg = json.loads(gen_cfg_path.read_text(encoding="utf-8"))
            self._decoder_start_id = int(gen_cfg.get("decoder_start_token_id", 2))
            self._eos_id = int(gen_cfg.get("eos_token_id", 2))

        # 4. Session options for CPU optimization
        sess_opts = ort.SessionOptions()
        sess_opts.graph_optimization_level = ort.GraphOptimizationLevel.ORT_ENABLE_ALL
        sess_opts.intra_op_num_threads = min(4, os.cpu_count() or 1)

        # 5. Load ONNX sessions
        self._enc_session = ort.InferenceSession(
            str(snap / "encoder_model.onnx"),
            sess_options=sess_opts,
            providers=self._providers,
        )
        self._dec_session = ort.InferenceSession(
            str(snap / "decoder_model.onnx"),
            sess_options=sess_opts,
            providers=self._providers,
        )
        self._dec_past_session = ort.InferenceSession(
            str(snap / "decoder_with_past_model.onnx"),
            sess_options=sess_opts,
            providers=self._providers,
        )

        # Calculate number of decoder layers for KV cache
        # Each layer has 4 past KV outputs (decoder.key, decoder.value, encoder.key, encoder.value)
        # plus 1 logits output
        total_dec_outputs = len(self._dec_session.get_outputs())
        self._num_layers = (total_dec_outputs - 1) // 4

        logger.info(
            "IndicTrans2 ONNX loaded successfully (layers=%d, providers=%s)",
            self._num_layers,
            self._providers,
        )

    def unload(self) -> None:
        """Unload all ONNX sessions and tokenizers to reclaim memory."""
        self._enc_session = None
        self._dec_session = None
        self._dec_past_session = None
        self._src_tokenizer = None
        self._tgt_tokenizer = None
        self._indic_processor = None
        self._meta = None
        gc.collect()
        logger.info("IndicTrans2 ONNX sessions unloaded and memory reclaimed.")

    def _resolve_model_dir(self) -> Path:
        """Locate model directory on local disk or download from Hugging Face."""
        candidate = Path(self._model_path_or_repo)
        if candidate.is_dir() and (candidate / "encoder_model.onnx").exists():
            self._local_snapshot_dir = candidate
            return candidate

        # Download or load from Hugging Face hub cache
        from huggingface_hub import snapshot_download

        local_dir = snapshot_download(
            repo_id=self._model_path_or_repo,
            allow_patterns=[
                "*.onnx",
                "*.onnx.data",
                "*.json",
                "model.*",
            ],
        )
        self._local_snapshot_dir = Path(local_dir)
        return self._local_snapshot_dir

    def get_components(self) -> Tuple[Any, Any, Any, Any, Any, Any, int, int, int]:
        """Return loaded sessions and tokenizers, ensuring model is loaded."""
        if not self.is_loaded:
            self.load()
        return (
            self._enc_session,
            self._dec_session,
            self._dec_past_session,
            self._src_tokenizer,
            self._tgt_tokenizer,
            self._indic_processor,
            self._decoder_start_id,
            self._eos_id,
            self._num_layers,
        )
