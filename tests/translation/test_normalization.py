"""Unit tests for text normalization, script validation, and punctuation standardization."""

from tribetalk.translation.normalization import TextNormalizer


class TestTextNormalizer:
    """Test suite for TextNormalizer."""

    def test_contains_devanagari(self) -> None:
        assert TextNormalizer.contains_devanagari("नमस्ते") is True
        assert TextNormalizer.contains_devanagari("भारत 123") is True
        assert TextNormalizer.contains_devanagari("Hello World") is False
        assert TextNormalizer.contains_devanagari("ᱡᱚᱦᱟᱨ") is False

    def test_contains_ol_chiki(self) -> None:
        assert TextNormalizer.contains_ol_chiki("ᱡᱚᱦᱟᱨ") is True
        assert TextNormalizer.contains_ol_chiki("ᱥᱟᱱᱛᱟᱲᱤ") is True
        assert TextNormalizer.contains_ol_chiki("नमस्ते") is False
        assert TextNormalizer.contains_ol_chiki("12345") is False

    def test_validate_script_for_language(self) -> None:
        assert TextNormalizer.validate_script_for_language("कल छुट्टी है।", "hi") is True
        assert TextNormalizer.validate_script_for_language("कल छुट्टी है।", "hin_Deva") is True
        assert TextNormalizer.validate_script_for_language("ᱜᱟᱯᱟ ᱡᱤᱨᱟᱹᱣ ᱢᱟᱸᱦᱟ ᱠᱟᱱᱟ", "sat") is True
        assert TextNormalizer.validate_script_for_language("ᱜᱟᱯᱟ ᱡᱤᱨᱟᱹᱣ ᱢᱟᱸᱦᱟ ᱠᱟᱱᱟ", "sat_Olck") is True

        # Mismatch
        assert TextNormalizer.validate_script_for_language("कल छुट्टी है।", "sat") is False
        assert TextNormalizer.validate_script_for_language("ᱜᱟᱯᱟ ᱡᱤᱨᱟᱹᱣ ᱢᱟᱸᱦᱟ ᱠᱟᱱᱟ", "hi") is False

    def test_normalize_input_whitespace_and_quotes(self) -> None:
        raw = "  “नमस्ते” ,   आप \t कैसे   हैं?  "
        cleaned = TextNormalizer.normalize_input(raw)
        assert cleaned == '"नमस्ते" , आप कैसे हैं?'

    def test_normalize_output_ol_chiki_punctuation(self) -> None:
        # Period at end of Ol Chiki text is converted to Ol Chiki Danda ᱾
        raw = "ᱜᱟᱯᱟ ᱡᱤᱨᱟᱹᱣ ᱢᱟᱸᱦᱟ ᱠᱟᱱᱟ."
        normalized = TextNormalizer.normalize_output(raw, "sat")
        assert normalized.endswith("᱾")
        assert "ᱜᱟᱯᱟ" in normalized

    def test_normalize_output_devanagari_punctuation(self) -> None:
        # Period at end of Devanagari text is converted to Danda ।
        raw = "कल छुट्टी है."
        normalized = TextNormalizer.normalize_output(raw, "hi")
        assert normalized.endswith("।")
        assert "कल" in normalized
