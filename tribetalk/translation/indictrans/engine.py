"""IndicTrans2 translation engine implementing TranslationInterface."""

from collections import OrderedDict
import threading
from pathlib import Path
from typing import Optional, Union, List, Tuple, Dict
import time
import numpy as np
import logging

from tribetalk.translation.common.interface import TranslationInterface
from tribetalk.translation.common.types import TranslationResult, LanguageTag
from tribetalk.translation.normalization import TextNormalizer
from tribetalk.translation.indictrans.model import IndicTransModelManager

logger = logging.getLogger(__name__)

# Pre-computed key tuples for KV cache feeding (eliminates 72 string allocations per generated token)
_PAST_KEYS_CACHE: Dict[int, Tuple[str, ...]] = {}


def _get_past_keys(num_layers: int) -> Tuple[str, ...]:
    """Retrieve or pre-generate the past_key_values.* input keys tuple."""
    if num_layers not in _PAST_KEYS_CACHE:
        keys = []
        for i in range(num_layers):
            keys.append(f"past_key_values.{i}.decoder.key")
            keys.append(f"past_key_values.{i}.decoder.value")
            keys.append(f"past_key_values.{i}.encoder.key")
            keys.append(f"past_key_values.{i}.encoder.value")
        _PAST_KEYS_CACHE[num_layers] = tuple(keys)
    return _PAST_KEYS_CACHE[num_layers]


def _past_feed(past_outputs: List[np.ndarray], num_layers: int) -> dict:
    """Fast past_key_values feed builder using pre-cached string keys."""
    return dict(zip(_get_past_keys(num_layers), past_outputs))


class IndicTransEngine(TranslationInterface):
    """Bidirectional neural translation engine for Hindi and Santali using IndicTrans2 INT8 ONNX."""

    def __init__(
        self,
        model_path_or_repo: Optional[Union[str, Path]] = None,
        providers: Optional[List[str]] = None,
        lazy_load: bool = True,
        max_new_tokens: int = 128,
        cache_size: int = 2048,
    ) -> None:
        """Initialize translation engine.

        Args:
            model_path_or_repo: Local directory path or Hugging Face repo ID.
            providers: ONNX Runtime execution providers.
            lazy_load: If False, loads model immediately upon initialization.
            max_new_tokens: Maximum target tokens to generate per sentence (default 128).
            cache_size: Capacity of high-performance translation LRU cache.
        """
        self._mgr = IndicTransModelManager(
            model_path_or_repo=model_path_or_repo,
            providers=providers,
        )
        self._max_new_tokens = max_new_tokens
        self._max_cache_size = cache_size
        self._cache: OrderedDict[Tuple[str, str, str], str] = OrderedDict()
        self._cache_lock = threading.Lock()

        if not lazy_load:
            self._mgr.load()

    @property
    def cache_size(self) -> int:
        """Number of currently cached translation pairs."""
        with self._cache_lock:
            return len(self._cache)

    def clear_cache(self) -> None:
        """Clear all cached translation pairs."""
        with self._cache_lock:
            self._cache.clear()

    @property
    def is_loaded(self) -> bool:
        """Whether model is currently loaded in memory."""
        return self._mgr.is_loaded

    def load_model(self) -> None:
        """Explicitly load model sessions and tokenizers into memory."""
        self._mgr.load()

    def unload_model(self) -> None:
        """Explicitly release model memory."""
        self._mgr.unload()

    def translate(
        self,
        text: str,
        source_language: str,
        target_language: str,
    ) -> TranslationResult:
        """Translate a single sentence between Hindi and Santali.

        Args:
            text: Input sentence in source script.
            source_language: Source language alias ('hi' or 'sat').
            target_language: Target language alias ('sat' or 'hi').

        Returns:
            TranslationResult containing translated text and metrics.
        """
        results = self.translate_batch(
            texts=[text],
            source_language=source_language,
            target_language=target_language,
        )
        return results[0]

    def translate_batch(
        self,
        texts: List[str],
        source_language: str,
        target_language: str,
    ) -> List[TranslationResult]:
        """Translate a batch of sentences between Hindi and Santali.

        Args:
            texts: List of sentences to translate.
            source_language: Source language alias ('hi' or 'sat').
            target_language: Target language alias ('sat' or 'hi').

        Returns:
            List of TranslationResult instances.
        """
        if not texts:
            return []

        # Validate and canonicalize language tags
        src_canonical = LanguageTag.to_canonical(source_language)
        tgt_canonical = LanguageTag.to_canonical(target_language)
        src_short = LanguageTag.to_short(source_language)
        tgt_short = LanguageTag.to_short(target_language)

        if src_canonical == tgt_canonical:
            raise ValueError(
                f"Source and target languages must be different. Got: {source_language!r}"
            )

        # Pre-normalize input text strings
        cleaned_inputs = [TextNormalizer.normalize_input(t) for t in texts]

        # Retrieve loaded components
        (
            enc_sess,
            dec_sess,
            dec_past_sess,
            src_tok,
            tgt_tok,
            ip,
            decoder_start_id,
            eos_id,
            num_layers,
        ) = self._mgr.get_components()

        meta = self._mgr._meta or {
            "src_dict_size": 256000,
            "tgt_dict_size": 256000,
            "unk_id": 3,
        }
        src_dict_size = meta.get("src_dict_size", 256000)
        tgt_dict_size = meta.get("tgt_dict_size", 256000)
        unk_id = meta.get("unk_id", 3)

        results: List[TranslationResult] = []

        for raw_in, cleaned_in in zip(texts, cleaned_inputs):
            if not cleaned_in:
                # Handle empty input gracefully
                results.append(
                    TranslationResult(
                        source_text=raw_in,
                        target_text="",
                        source_language=src_short,
                        target_language=tgt_short,
                        processing_time_ms=0.0,
                    )
                )
                continue

            cache_key = (cleaned_in, src_short, tgt_short)
            with self._cache_lock:
                if cache_key in self._cache:
                    cached_target = self._cache[cache_key]
                    self._cache.move_to_end(cache_key)
                    results.append(
                        TranslationResult(
                            source_text=raw_in,
                            target_text=cached_target,
                            source_language=src_short,
                            target_language=tgt_short,
                            processing_time_ms=0.01,
                        )
                    )
                    continue

            t0 = time.perf_counter()

            # 1. IndicProcessor preprocessing (injects language tags and normalizes script)
            if hasattr(ip, "_placeholder_entity_maps"):
                ip._placeholder_entity_maps.queue.clear()

            prefixed_list = ip.preprocess_batch(
                [cleaned_in], src_lang=src_canonical, tgt_lang=tgt_canonical
            )
            prefixed = prefixed_list[0]

            # 2. Tokenize source sequence
            encoded = src_tok.encode(prefixed)
            input_ids = np.array(
                [[i if i < src_dict_size else unk_id for i in encoded.ids]],
                dtype=np.int64,
            )
            attn_mask = np.array([encoded.attention_mask], dtype=np.int64)

            # 3. Encoder forward pass
            enc_out: np.ndarray = enc_sess.run(
                ["last_hidden_state"],
                {"input_ids": input_ids, "attention_mask": attn_mask},
            )[0]

            # 4. Autoregressive greedy decoding with KV caching
            decoder_input_ids = np.array([[decoder_start_id]], dtype=np.int64)
            output_ids: List[int] = [decoder_start_id]
            past_outputs: Optional[List[np.ndarray]] = None

            for step in range(self._max_new_tokens):
                if step == 0:
                    dec_out = dec_sess.run(
                        None,
                        {
                            "input_ids": decoder_input_ids,
                            "encoder_hidden_states": enc_out,
                            "encoder_attention_mask": attn_mask,
                        },
                    )
                else:
                    dec_out = dec_past_sess.run(
                        None,
                        {
                            "input_ids": decoder_input_ids,
                            "encoder_attention_mask": attn_mask,
                            **_past_feed(past_outputs, num_layers),
                        },
                    )

                logits: np.ndarray = dec_out[0]
                past_outputs = list(dec_out[1:])
                next_id = int(np.argmax(logits[0, -1, :]))
                output_ids.append(next_id)

                if next_id == eos_id:
                    break

                decoder_input_ids = np.array([[next_id]], dtype=np.int64)

            # 5. Decode token IDs to text in internal Devanagari space
            safe_ids = [i if i < tgt_dict_size else unk_id for i in output_ids]
            raw_decoded = tgt_tok.decode(safe_ids, skip_special_tokens=True)

            # 6. IndicProcessor postprocessing (converts back to target script)
            postprocessed = ip.postprocess_batch([raw_decoded], lang=tgt_canonical)
            translated_raw = postprocessed[0]

            # 7. Final text cleanup and punctuation harmonization
            final_text = TextNormalizer.normalize_output(translated_raw, tgt_short)

            latency_ms = (time.perf_counter() - t0) * 1000.0

            # Store into thread-safe LRU cache
            with self._cache_lock:
                self._cache[cache_key] = final_text
                if len(self._cache) > self._max_cache_size:
                    self._cache.popitem(last=False)

            results.append(
                TranslationResult(
                    source_text=raw_in,
                    target_text=final_text,
                    source_language=src_short,
                    target_language=tgt_short,
                    processing_time_ms=round(latency_ms, 2),
                )
            )

        return results
