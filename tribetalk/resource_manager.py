"""Global Model Resource Manager for TribeTalk.

Coordinates memory-constrained model lifecycles across ASR, Translation, and TTS.
Enforces memory budgets suitable for low-end hardware (e.g. 3-4 GB Android tablets)
by providing sequential leasing, LRU caching, and emergency memory trimming.
"""

from __future__ import annotations

import gc
import logging
import threading
from contextlib import contextmanager
from enum import Enum
from typing import Any, Callable, Dict, Iterator, List, Optional, Union

from tribetalk.asr import ASRInterface, get_asr
from tribetalk.translation import TranslationInterface, get_translator
from tribetalk.tts import TTSInterface, get_tts

logger = logging.getLogger(__name__)


class MemoryMode(str, Enum):
    """Memory management operational modes.
    
    SEQUENTIAL:
        Strictly allows only ONE model family in active memory at a time.
        Before a new model is acquired, currently loaded models are unloaded.
        Recommended for low-end devices (RAM <= 4 GB) to stay within the <= 950 MB ML limit.
    CACHED:
        Retains loaded models up to `max_cached_models` using an LRU policy.
        Useful for desktop, evaluation passes, or higher-RAM devices.
    """
    SEQUENTIAL = "sequential"
    CACHED = "cached"


class GlobalModelResourceManager:
    """Manages creation, caching, leasing, and eviction of ML models.
    
    Guarantees thread-safe access and strict enforcement of the system RAM budget.
    """

    def __init__(
        self,
        mode: Union[MemoryMode, str] = MemoryMode.SEQUENTIAL,
        max_cached_models: int = 1,
        asr_factory: Optional[Callable[[str], ASRInterface]] = None,
        translation_factory: Optional[Callable[[], TranslationInterface]] = None,
        tts_factory: Optional[Callable[[str], TTSInterface]] = None,
    ) -> None:
        """Initialize the GlobalModelResourceManager.
        
        Args:
            mode: MemoryMode.SEQUENTIAL or MemoryMode.CACHED.
            max_cached_models: Maximum loaded models in CACHED mode.
            asr_factory: Factory function for ASR engines.
            translation_factory: Factory function for Translation engine.
            tts_factory: Factory function for TTS engines.
        """
        self.mode = MemoryMode(mode) if isinstance(mode, str) else mode
        self.max_cached_models = max(1, max_cached_models)

        self._asr_factory = asr_factory or get_asr
        self._translation_factory = translation_factory or get_translator
        self._tts_factory = tts_factory or get_tts

        self._lock = threading.RLock()
        # Pool of instantiated engines: key -> engine instance
        self._pool: Dict[str, Any] = {}
        # Order of usage for LRU eviction: list of keys (most recently used at end)
        self._lru_order: List[str] = []

    # -------------------------------------------------------------------------
    # Key normalization helpers
    # -------------------------------------------------------------------------

    @staticmethod
    def _make_asr_key(language: str) -> str:
        lang = "hi" if language.lower() in ("hi", "hindi", "hin") else "sat"
        return f"asr:{lang}"

    @staticmethod
    def _make_translation_key() -> str:
        return "translation"

    @staticmethod
    def _make_tts_key(language: str) -> str:
        lang = "hi" if language.lower() in ("hi", "hindi", "hin") else "sat"
        return f"tts:{lang}"

    # -------------------------------------------------------------------------
    # Core lifecycle methods
    # -------------------------------------------------------------------------

    def _mark_used(self, key: str) -> None:
        """Record usage of key for LRU ordering."""
        if key in self._lru_order:
            self._lru_order.remove(key)
        self._lru_order.append(key)

    def _evict_lru_if_needed(self, required_spare: int = 1) -> None:
        """Evicts models if number of loaded models exceeds capacity."""
        with self._lock:
            if self.mode == MemoryMode.SEQUENTIAL:
                # In sequential mode, unload everything currently loaded
                for key, engine in list(self._pool.items()):
                    if getattr(engine, "is_loaded", False):
                        logger.info(f"[ResourceManager] Evicting model {key} (SEQUENTIAL mode)")
                        if hasattr(engine, "unload_model") and callable(getattr(engine, "unload_model")):
                            engine.unload_model()
                self._lru_order.clear()
            else:
                # CACHED mode: evict oldest if at or above capacity
                while len(self.get_active_models()) >= (self.max_cached_models - required_spare + 1):
                    evicted = False
                    for key in list(self._lru_order):
                        engine = self._pool.get(key)
                        if engine and getattr(engine, "is_loaded", False):
                            logger.info(f"[ResourceManager] Evicting LRU model {key} (CACHED mode)")
                            if hasattr(engine, "unload_model") and callable(getattr(engine, "unload_model")):
                                engine.unload_model()
                            self._lru_order.remove(key)
                            evicted = True
                            break
                    if not evicted:
                        break

            gc.collect()

    def acquire_asr(self, language: str) -> ASRInterface:
        """Acquire an ASR engine for the given language."""
        key = self._make_asr_key(language)
        with self._lock:
            if key not in self._pool:
                lang_code = key.split(":")[1]
                self._pool[key] = self._asr_factory(lang_code, lazy_load=True)

            engine: ASRInterface = self._pool[key]
            if not engine.is_loaded:
                self._evict_lru_if_needed(required_spare=1)
                logger.info(f"[ResourceManager] Loading {key}")
                engine.load_model()

            self._mark_used(key)
            return engine

    def acquire_translation(self) -> TranslationInterface:
        """Acquire the bidirectional IndicTrans2 translation engine."""
        key = self._make_translation_key()
        with self._lock:
            if key not in self._pool:
                self._pool[key] = self._translation_factory(lazy_load=True)

            engine: TranslationInterface = self._pool[key]
            if not engine.is_loaded:
                self._evict_lru_if_needed(required_spare=1)
                logger.info(f"[ResourceManager] Loading {key}")
                engine.load_model()

            self._mark_used(key)
            return engine

    def acquire_tts(self, language: str) -> TTSInterface:
        """Acquire a TTS engine for the given language."""
        key = self._make_tts_key(language)
        with self._lock:
            if key not in self._pool:
                lang_code = key.split(":")[1]
                self._pool[key] = self._tts_factory(lang_code, lazy_load=True)

            engine: TTSInterface = self._pool[key]
            if not engine.is_loaded:
                self._evict_lru_if_needed(required_spare=1)
                logger.info(f"[ResourceManager] Loading {key}")
                engine.load_model()

            self._mark_used(key)
            return engine

    def release(self, key: str) -> None:
        """Release (unload) a specific engine by key."""
        with self._lock:
            engine = self._pool.get(key)
            if engine and getattr(engine, "is_loaded", False):
                logger.info(f"[ResourceManager] Explicit release of {key}")
                if hasattr(engine, "unload_model") and callable(getattr(engine, "unload_model")):
                    engine.unload_model()
            if key in self._lru_order:
                self._lru_order.remove(key)
            gc.collect()

    def release_all(self) -> None:
        """Release and unload all models in the pool."""
        with self._lock:
            for key, engine in list(self._pool.items()):
                if getattr(engine, "is_loaded", False):
                    logger.info(f"[ResourceManager] Unloading {key}")
                    if hasattr(engine, "unload_model") and callable(getattr(engine, "unload_model")):
                        engine.unload_model()
            self._lru_order.clear()
            gc.collect()

    def trim_memory(self) -> None:
        """Emergency memory trim (analogous to Android onTrimMemory).
        
        Unloads all models and clears internal pools.
        """
        with self._lock:
            logger.warning("[ResourceManager] trim_memory triggered: emergency release of all models")
            self.release_all()
            self._pool.clear()
            gc.collect()

    def get_active_models(self) -> List[str]:
        """Return list of currently loaded model keys."""
        with self._lock:
            return [
                key for key, engine in self._pool.items()
                if getattr(engine, "is_loaded", False)
            ]

    # -------------------------------------------------------------------------
    # Context managers for safe single-stage leasing
    # -------------------------------------------------------------------------

    @contextmanager
    def lease_asr(self, language: str) -> Iterator[ASRInterface]:
        """Context manager leasing ASR engine. Automatically releases if SEQUENTIAL."""
        key = self._make_asr_key(language)
        engine = self.acquire_asr(language)
        try:
            yield engine
        finally:
            if self.mode == MemoryMode.SEQUENTIAL:
                self.release(key)

    @contextmanager
    def lease_translation(self) -> Iterator[TranslationInterface]:
        """Context manager leasing Translation engine."""
        key = self._make_translation_key()
        engine = self.acquire_translation()
        try:
            yield engine
        finally:
            if self.mode == MemoryMode.SEQUENTIAL:
                self.release(key)

    @contextmanager
    def lease_tts(self, language: str) -> Iterator[TTSInterface]:
        """Context manager leasing TTS engine."""
        key = self._make_tts_key(language)
        engine = self.acquire_tts(language)
        try:
            yield engine
        finally:
            if self.mode == MemoryMode.SEQUENTIAL:
                self.release(key)
