"""Hindi IndicConformer ASR model loader and device manager."""

from pathlib import Path
from typing import Optional, Union
import logging
import torch

# Import NeMo ASR first so that base classes and registries are fully initialized
import nemo.collections.asr as nemo_asr

logger = logging.getLogger(__name__)

DEFAULT_HINDI_MODEL_REPO: str = "ai4bharat/indicconformer_stt_hi_hybrid_ctc_rnnt_large"
DEFAULT_HINDI_MODEL_FILE: str = "indicconformer_stt_hi_hybrid_rnnt_large.nemo"

_PATCHES_APPLIED: bool = False


def _apply_nemo_compatibility_patches() -> None:
    """Apply NeMo compatibility patches for AI4Bharat multilingual IndicConformer.

    AI4Bharat trained IndicConformer using custom multilingual extensions
    (e.g., 'multilingual' tokenizer type, multilingual RNNT and CTC multi-softmax parameters,
    and per-language projection layers). These patches bridge those checkpoints cleanly
    with standard NeMo 3.x releases without altering weight values or model math.
    """
    global _PATCHES_APPLIED
    if _PATCHES_APPLIED:
        return

    import nemo.collections.asr.parts.mixins.mixins as m
    from omegaconf import DictConfig
    from nemo.collections.asr.modules.rnnt import RNNTDecoder, RNNTJoint
    from nemo.collections.asr.modules.conv_asr import ConvASRDecoder
    from nemo.core.connectors.save_restore_connector import SaveRestoreConnector
    import nemo.collections.common.tokenizers.aggregate_tokenizer as agg
    from nemo.collections.common.tokenizers.aggregate_tokenizer import AggregateTokenizer
    import nemo.collections.common.tokenizers.sentencepiece_tokenizer as sp_tok

    # 1. Normalize 'multilingual' tokenizer type to aggregate 'agg'
    orig_setup_tokenizer = m.ASRBPEMixin._setup_tokenizer

    def patched_setup_tokenizer(self, tokenizer_cfg: DictConfig):
        if tokenizer_cfg.get("type") == "multilingual":
            tokenizer_cfg["type"] = "agg"
        return orig_setup_tokenizer(self, tokenizer_cfg)

    m.ASRBPEMixin._setup_tokenizer = patched_setup_tokenizer

    # 2. Accommodate custom decoder arguments from AI4Bharat checkpoints
    orig_rnnt_init = RNNTDecoder.__init__

    def patched_rnnt_init(self, *args, **kwargs):
        kwargs.pop("multisoftmax", None)
        return orig_rnnt_init(self, *args, **kwargs)

    RNNTDecoder.__init__ = patched_rnnt_init

    orig_joint_init = RNNTJoint.__init__

    def patched_joint_init(self, *args, **kwargs):
        kwargs.pop("multilingual", None)
        kwargs.pop("language_keys", None)
        return orig_joint_init(self, *args, **kwargs)

    RNNTJoint.__init__ = patched_joint_init

    orig_conv_init = ConvASRDecoder.__init__

    def patched_conv_init(self, *args, **kwargs):
        kwargs.pop("multisoftmax", None)
        return orig_conv_init(self, *args, **kwargs)

    ConvASRDecoder.__init__ = patched_conv_init

    # 3. Use strict=False so checkpoint language-specific prediction heads load cleanly
    orig_load_instance = SaveRestoreConnector.load_instance_with_state_dict

    def patched_load_instance(self, instance, state_dict, strict):
        return orig_load_instance(self, instance, state_dict, strict=False)

    SaveRestoreConnector.load_instance_with_state_dict = patched_load_instance

    # 4. Default transcription language to Hindi ('hi') for AggregateTokenizer
    def _patched_call_agg_tokenizer(self, text: str, lang: str | None = None):
        if lang is None:
            lang = getattr(self, "default_lang", "hi")
        return self._tokenizer.text_to_ids(text, lang)

    agg.TokenizerWrapper._call_agg_tokenizer = _patched_call_agg_tokenizer

    orig_tokens_to_text = AggregateTokenizer.tokens_to_text

    def patched_tokens_to_text(self, tokens, lang_id=None):
        if lang_id is None:
            lang_id = getattr(self, "default_lang", "hi")
        return orig_tokens_to_text(self, tokens, lang_id)

    AggregateTokenizer.tokens_to_text = patched_tokens_to_text

    # 5. Provide decode_pieces on SentencePieceTokenizer
    sp_tok.SentencePieceTokenizer.decode_pieces = lambda self, tokens: self.tokens_to_text(tokens)

    _PATCHES_APPLIED = True
    logger.debug("NeMo IndicConformer compatibility patches applied successfully.")


# Apply compatibility patches once upon importing the Hindi ASR model module
_apply_nemo_compatibility_patches()


class HindiASRModel:
    """Manages loading, caching, and device placement for Hindi IndicConformer."""

    def __init__(
        self,
        model_path_or_repo: Optional[Union[str, Path]] = None,
        device: Optional[str] = None,
        decoder: str = "ctc",
    ) -> None:
        """Initialize model configuration.

        Args:
            model_path_or_repo: Local .nemo file path, or Hugging Face repo ID.
                If None, uses DEFAULT_HINDI_MODEL_REPO.
            device: 'cuda', 'cpu', or None (auto-detect CUDA availability).
            decoder: 'ctc' or 'rnnt'. Default is 'ctc'.
        """
        self._model_path_or_repo = model_path_or_repo or DEFAULT_HINDI_MODEL_REPO
        if device is None:
            self._device = "cuda" if torch.cuda.is_available() else "cpu"
        else:
            self._device = device

        if decoder.lower() not in ("ctc", "rnnt"):
            raise ValueError(f"decoder must be 'ctc' or 'rnnt', got {decoder!r}")
        self._decoder = decoder.lower()

        self._model = None
        self._local_nemo_path: Optional[Path] = None

    @property
    def is_loaded(self) -> bool:
        """Check if model is currently loaded in memory."""
        return self._model is not None

    @property
    def device(self) -> str:
        """Device on which the model is loaded ('cuda' or 'cpu')."""
        return self._device

    @property
    def decoder(self) -> str:
        """Active decoder ('ctc' or 'rnnt')."""
        return self._decoder

    @decoder.setter
    def decoder(self, value: str) -> None:
        if value.lower() not in ("ctc", "rnnt"):
            raise ValueError(f"decoder must be 'ctc' or 'rnnt', got {value!r}")
        self._decoder = value.lower()
        if self._model is not None:
            self._model.cur_decoder = self._decoder

    def load(self) -> None:
        """Load the IndicConformer model into memory.

        Idempotent: does nothing if already loaded.
        """
        if self._model is not None:
            return

        _apply_nemo_compatibility_patches()

        resolved_path = self._resolve_model_path()
        logger.info(f"Loading Hindi IndicConformer model from: {resolved_path}")

        model = nemo_asr.models.ASRModel.restore_from(str(resolved_path))
        model.freeze()
        model = model.to(torch.device(self._device))
        model.cur_decoder = self._decoder

        self._model = model
        logger.info(f"Hindi IndicConformer loaded successfully on {self._device} (decoder={self._decoder})")

    def _resolve_model_path(self) -> Path:
        """Resolve model to a local .nemo file path, downloading if necessary."""
        candidate = Path(self._model_path_or_repo)
        if candidate.is_file() and candidate.suffix == ".nemo":
            self._local_nemo_path = candidate
            return candidate

        # If it's a directory containing the .nemo file
        if candidate.is_dir():
            nemo_files = list(candidate.glob("*.nemo"))
            if nemo_files:
                self._local_nemo_path = nemo_files[0]
                return nemo_files[0]

        # Download or resolve from Hugging Face cache
        from huggingface_hub import hf_hub_download

        repo_id = str(self._model_path_or_repo)
        local_path = hf_hub_download(
            repo_id=repo_id,
            filename=DEFAULT_HINDI_MODEL_FILE,
        )
        self._local_nemo_path = Path(local_path)
        return self._local_nemo_path

    def get_model(self):
        """Return loaded NeMo ASR model, loading it if not yet loaded."""
        if self._model is None:
            self.load()
        return self._model

    def unload(self) -> None:
        """Unload model weights and release memory."""
        if self._model is not None:
            del self._model
            self._model = None
            if torch.cuda.is_available():
                torch.cuda.empty_cache()
            import gc
            gc.collect()
            logger.info("Hindi IndicConformer unloaded successfully.")

