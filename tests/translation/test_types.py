"""Unit tests for translation data structures and language code mapping."""

import pytest
from tribetalk.translation.common.types import TranslationResult, LanguageTag


class TestTranslationResult:
    """Test suite for TranslationResult dataclass."""

    def test_valid_translation_result(self) -> None:
        res = TranslationResult(
            source_text="नमस्ते",
            target_text="ᱡᱚᱦᱟᱨ",
            source_language="hi",
            target_language="sat",
            processing_time_ms=85.5,
        )
        assert res.source_text == "नमस्ते"
        assert res.target_text == "ᱡᱚᱦᱟᱨ"
        assert res.source_language == "hi"
        assert res.target_language == "sat"
        assert res.processing_time_ms == 85.5
        assert res.is_final is True
        assert res.model_name == "IndicTrans2-320M-ONNX-INT8"

    def test_immutability(self) -> None:
        res = TranslationResult(
            source_text="hello",
            target_text="world",
            source_language="hi",
            target_language="sat",
            processing_time_ms=10.0,
        )
        with pytest.raises(AttributeError):
            res.target_text = "changed"  # type: ignore

    def test_to_dict_serialization(self) -> None:
        res = TranslationResult(
            source_text="कल छुट्टी है",
            target_text="ᱜᱟᱯᱟ ᱡᱤᱨᱟᱹᱣ ᱢᱟᱸᱦᱟ ᱠᱟᱱᱟ",
            source_language="hi",
            target_language="sat",
            processing_time_ms=120.0,
        )
        d = res.to_dict()
        assert isinstance(d, dict)
        assert d["source_text"] == "कल छुट्टी है"
        assert d["target_text"] == "ᱜᱟᱯᱟ ᱡᱤᱨᱟᱹᱣ ᱢᱟᱸᱦᱟ ᱠᱟᱱᱟ"
        assert d["source_language"] == "hi"
        assert d["target_language"] == "sat"
        assert d["processing_time_ms"] == 120.0

    def test_invalid_text_type_raises(self) -> None:
        with pytest.raises(TypeError):
            TranslationResult(
                source_text=123,  # type: ignore
                target_text="valid",
                source_language="hi",
                target_language="sat",
                processing_time_ms=10.0,
            )

    def test_empty_language_raises(self) -> None:
        with pytest.raises(ValueError):
            TranslationResult(
                source_text="test",
                target_text="test",
                source_language="",
                target_language="sat",
                processing_time_ms=10.0,
            )

    def test_negative_latency_raises(self) -> None:
        with pytest.raises(ValueError):
            TranslationResult(
                source_text="test",
                target_text="test",
                source_language="hi",
                target_language="sat",
                processing_time_ms=-5.0,
            )


class TestLanguageTag:
    """Test suite for LanguageTag mapping and validation."""

    def test_hindi_aliases(self) -> None:
        assert LanguageTag.to_canonical("hi") == "hin_Deva"
        assert LanguageTag.to_canonical("hindi") == "hin_Deva"
        assert LanguageTag.to_canonical("hin") == "hin_Deva"
        assert LanguageTag.to_canonical("hin_Deva") == "hin_Deva"
        assert LanguageTag.to_canonical("HINDI") == "hin_Deva"

    def test_santali_aliases(self) -> None:
        assert LanguageTag.to_canonical("sat") == "sat_Olck"
        assert LanguageTag.to_canonical("santali") == "sat_Olck"
        assert LanguageTag.to_canonical("santhali") == "sat_Olck"
        assert LanguageTag.to_canonical("olchiki") == "sat_Olck"
        assert LanguageTag.to_canonical("sat_Olck") == "sat_Olck"
        assert LanguageTag.to_canonical("SANTALI") == "sat_Olck"

    def test_to_short(self) -> None:
        assert LanguageTag.to_short("hin_Deva") == "hi"
        assert LanguageTag.to_short("hindi") == "hi"
        assert LanguageTag.to_short("sat_Olck") == "sat"
        assert LanguageTag.to_short("santali") == "sat"

    def test_unsupported_language_raises(self) -> None:
        with pytest.raises(ValueError, match="Unsupported translation language"):
            LanguageTag.to_canonical("fr")
        with pytest.raises(ValueError, match="Unsupported translation language"):
            LanguageTag.to_canonical("english")

    def test_is_supported(self) -> None:
        assert LanguageTag.is_supported("hi") is True
        assert LanguageTag.is_supported("sat") is True
        assert LanguageTag.is_supported("hindi") is True
        assert LanguageTag.is_supported("santali") is True
        assert LanguageTag.is_supported("german") is False
