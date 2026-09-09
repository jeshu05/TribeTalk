"""Unit tests for TTS interface contract, lifecycle, and factory function."""

import pytest
from tribetalk.tts.common.interface import TTSInterface
from tribetalk.tts import get_tts, HindiTTS, SantaliTTS


class TestTTSInterfaceContract:
    """Test suite for TTSInterface ABC contracts."""

    def test_cannot_instantiate_abc(self) -> None:
        with pytest.raises(TypeError):
            TTSInterface()  # type: ignore

    def test_subclass_must_implement_all_abstract_methods(self) -> None:
        class IncompleteTTS(TTSInterface):
            pass

        with pytest.raises(TypeError):
            IncompleteTTS()  # type: ignore


class TestTTSEngineLifecycle:
    """Test suite for engine lifecycle and factory routing."""

    def test_hindi_tts_is_subclass(self) -> None:
        assert issubclass(HindiTTS, TTSInterface)

    def test_santali_tts_is_subclass(self) -> None:
        assert issubclass(SantaliTTS, TTSInterface)

    def test_hindi_tts_properties(self) -> None:
        engine = HindiTTS(lazy_load=True)
        assert engine.language == "hi"
        assert engine.sample_rate == 16000
        assert engine.is_loaded is False

    def test_santali_tts_properties(self) -> None:
        engine = SantaliTTS(lazy_load=True)
        assert engine.language == "sat"
        assert engine.sample_rate == 16000
        assert engine.is_loaded is False

    def test_santali_tts_lifecycle(self) -> None:
        engine = SantaliTTS(lazy_load=True)
        assert engine.is_loaded is False

        # Load
        engine.load_model()
        assert engine.is_loaded is True

        # Unload
        engine.unload_model()
        assert engine.is_loaded is False

        # Idempotent unload
        engine.unload_model()
        assert engine.is_loaded is False

    def test_factory_get_tts_hindi_aliases(self) -> None:
        for alias in ["hi", "hindi", "hin", "HINDI"]:
            engine = get_tts(alias, lazy_load=True)
            assert isinstance(engine, HindiTTS)
            assert engine.language == "hi"
            assert engine.sample_rate == 16000

    def test_factory_get_tts_santali_aliases(self) -> None:
        for alias in ["sat", "santali", "santhali", "olchiki", "SANTALI"]:
            engine = get_tts(alias, lazy_load=True)
            assert isinstance(engine, SantaliTTS)
            assert engine.language == "sat"
            assert engine.sample_rate == 16000

    def test_factory_unsupported_language_raises(self) -> None:
        with pytest.raises(ValueError, match="Unsupported TTS language"):
            get_tts("bengali")
        with pytest.raises(ValueError, match="Unsupported TTS language"):
            get_tts("en")
