"""Unit tests for translation interface contract, lifecycle, and factory function."""

import pytest
from tribetalk.translation.common.interface import TranslationInterface
from tribetalk.translation import get_translator, IndicTransEngine


class TestTranslationInterfaceContract:
    """Test suite for TranslationInterface ABC contracts."""

    def test_cannot_instantiate_abc(self) -> None:
        with pytest.raises(TypeError):
            TranslationInterface()  # type: ignore

    def test_subclass_must_implement_all_abstract_methods(self) -> None:
        class IncompleteEngine(TranslationInterface):
            pass

        with pytest.raises(TypeError):
            IncompleteEngine()  # type: ignore


class TestTranslationEngineLifecycle:
    """Test suite for engine lifecycle and factory."""

    def test_engine_implements_interface(self) -> None:
        assert issubclass(IndicTransEngine, TranslationInterface)

    def test_lazy_loading_lifecycle(self) -> None:
        engine = IndicTransEngine(lazy_load=True)
        assert engine.is_loaded is False

        # Explicit load
        engine.load_model()
        assert engine.is_loaded is True

        # Explicit unload
        engine.unload_model()
        assert engine.is_loaded is False

        # Idempotent unload
        engine.unload_model()
        assert engine.is_loaded is False

    def test_factory_function(self) -> None:
        engine = get_translator(lazy_load=True)
        assert isinstance(engine, IndicTransEngine)
        assert isinstance(engine, TranslationInterface)
        assert engine.is_loaded is False

    def test_same_language_raises_value_error(self, translation_engine: TranslationInterface) -> None:
        with pytest.raises(ValueError, match="must be different"):
            translation_engine.translate("नमस्ते", source_language="hi", target_language="hi")

        with pytest.raises(ValueError, match="must be different"):
            translation_engine.translate("ᱡᱚᱦᱟᱨ", source_language="sat", target_language="santali")
