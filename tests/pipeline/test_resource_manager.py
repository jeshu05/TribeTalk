"""Unit tests for GlobalModelResourceManager."""

from unittest.mock import MagicMock
import pytest
from tribetalk.resource_manager import GlobalModelResourceManager, MemoryMode


class FakeEngine:
    """Lightweight test double for interface engines."""

    def __init__(self, name: str) -> None:
        self.name = name
        self._is_loaded = False

    @property
    def is_loaded(self) -> bool:
        return self._is_loaded

    def load_model(self) -> None:
        self._is_loaded = True

    def unload_model(self) -> None:
        self._is_loaded = False


class TestGlobalModelResourceManager:
    """Test suite for memory-aware model resource management."""

    def test_default_initialization(self) -> None:
        mgr = GlobalModelResourceManager()
        assert mgr.mode == MemoryMode.SEQUENTIAL
        assert mgr.max_cached_models == 1
        assert mgr.get_active_models() == []

    def test_sequential_mode_evicts_previous_model(self) -> None:
        """In SEQUENTIAL mode, acquiring engine B must unload engine A."""
        fake_asr = FakeEngine("asr_hi")
        fake_nmt = FakeEngine("nmt")

        mgr = GlobalModelResourceManager(
            mode=MemoryMode.SEQUENTIAL,
            asr_factory=lambda lang, **kw: fake_asr,
            translation_factory=lambda **kw: fake_nmt,
        )

        # 1. Acquire ASR
        engine1 = mgr.acquire_asr("hi")
        assert engine1.is_loaded is True
        assert mgr.get_active_models() == ["asr:hi"]

        # 2. Acquire Translation -> ASR must be evicted
        engine2 = mgr.acquire_translation()
        assert engine2.is_loaded is True
        assert fake_asr.is_loaded is False  # Previous model evicted
        assert mgr.get_active_models() == ["translation"]

    def test_cached_mode_respects_capacity(self) -> None:
        """In CACHED mode with capacity 2, acquiring a 3rd model evicts the LRU model."""
        engines = {
            "hi": FakeEngine("asr_hi"),
            "sat": FakeEngine("asr_sat"),
            "tts_hi": FakeEngine("tts_hi"),
        }

        mgr = GlobalModelResourceManager(
            mode=MemoryMode.CACHED,
            max_cached_models=2,
            asr_factory=lambda lang, **kw: engines[lang],
            tts_factory=lambda lang, **kw: engines["tts_hi"],
        )

        # 1. Load ASR Hindi
        mgr.acquire_asr("hi")
        assert mgr.get_active_models() == ["asr:hi"]

        # 2. Load ASR Santali
        mgr.acquire_asr("sat")
        assert set(mgr.get_active_models()) == {"asr:hi", "asr:sat"}

        # 3. Load TTS Hindi -> should evict LRU (asr:hi)
        mgr.acquire_tts("hi")
        assert engines["hi"].is_loaded is False
        assert set(mgr.get_active_models()) == {"asr:sat", "tts:hi"}

    def test_release_all(self) -> None:
        fake_asr = FakeEngine("asr_hi")
        fake_nmt = FakeEngine("nmt")

        mgr = GlobalModelResourceManager(
            mode=MemoryMode.CACHED,
            max_cached_models=5,
            asr_factory=lambda lang, **kw: fake_asr,
            translation_factory=lambda **kw: fake_nmt,
        )

        mgr.acquire_asr("hi")
        mgr.acquire_translation()
        assert len(mgr.get_active_models()) == 2

        mgr.release_all()
        assert fake_asr.is_loaded is False
        assert fake_nmt.is_loaded is False
        assert mgr.get_active_models() == []

    def test_trim_memory_clears_pool(self) -> None:
        fake_asr = FakeEngine("asr_hi")
        mgr = GlobalModelResourceManager(
            mode=MemoryMode.SEQUENTIAL,
            asr_factory=lambda lang, **kw: fake_asr,
        )
        mgr.acquire_asr("hi")
        assert fake_asr.is_loaded is True

        mgr.trim_memory()
        assert fake_asr.is_loaded is False
        assert mgr.get_active_models() == []
        assert len(mgr._pool) == 0

    def test_lease_context_manager_sequential(self) -> None:
        fake_asr = FakeEngine("asr_hi")
        mgr = GlobalModelResourceManager(
            mode=MemoryMode.SEQUENTIAL,
            asr_factory=lambda lang, **kw: fake_asr,
        )

        with mgr.lease_asr("hi") as asr:
            assert asr.is_loaded is True
            assert mgr.get_active_models() == ["asr:hi"]

        # Exiting context must automatically unload in SEQUENTIAL mode
        assert fake_asr.is_loaded is False
        assert mgr.get_active_models() == []
