"""Integration tests for genuine bidirectional Hindi <-> Santali translation.

Evaluates translation performance directly on reference sentences from the
frozen tribe-evaluation benchmark datasets (IN22-conv and IN22-gen).
"""

import pytest
from typing import List, Tuple
from tribetalk.translation import TranslationInterface, TranslationResult, TextNormalizer


@pytest.mark.integration
class TestBidirectionalTranslationInference:
    """Integration test suite executing genuine translation on official benchmark pairs."""

    def test_hindi_to_santali_in22_conv(
        self,
        translation_engine: TranslationInterface,
        in22_conv_pairs: List[Tuple[str, str]],
    ) -> None:
        """Evaluate Hindi -> Santali translation on IN22-conv conversational dialogue."""
        assert len(in22_conv_pairs) >= 5, "Expected at least 5 benchmark sentence pairs"

        # Evaluate on the first 3 conversational dialogue sentences
        for i in range(3):
            hi_source, sat_reference = in22_conv_pairs[i]

            result = translation_engine.translate(
                text=hi_source,
                source_language="hi",
                target_language="sat",
            )

            assert isinstance(result, TranslationResult)
            assert result.source_language == "hi"
            assert result.target_language == "sat"
            assert result.processing_time_ms > 0.0
            assert len(result.target_text.strip()) > 0

            # Must contain authentic Ol Chiki characters (U+1C50 to U+1C7F)
            assert TextNormalizer.contains_ol_chiki(result.target_text), (
                f"Expected Ol Chiki characters in translation output, got: {result.target_text!r} "
                f"(Source: {hi_source!r}, Reference: {sat_reference!r})"
            )

    def test_santali_to_hindi_in22_conv(
        self,
        translation_engine: TranslationInterface,
        in22_conv_pairs: List[Tuple[str, str]],
    ) -> None:
        """Evaluate Santali -> Hindi translation on IN22-conv conversational dialogue."""
        assert len(in22_conv_pairs) >= 5

        # Evaluate on the first 3 conversational dialogue sentences in reverse
        for i in range(3):
            hi_reference, sat_source = in22_conv_pairs[i]

            result = translation_engine.translate(
                text=sat_source,
                source_language="sat",
                target_language="hi",
            )

            assert isinstance(result, TranslationResult)
            assert result.source_language == "sat"
            assert result.target_language == "hi"
            assert result.processing_time_ms > 0.0
            assert len(result.target_text.strip()) > 0

            # Must contain authentic Devanagari characters (U+0900 to U+097F)
            assert TextNormalizer.contains_devanagari(result.target_text), (
                f"Expected Devanagari characters in translation output, got: {result.target_text!r} "
                f"(Source: {sat_source!r}, Reference: {hi_reference!r})"
            )

    def test_batch_translation_in22_gen(
        self,
        translation_engine: TranslationInterface,
        in22_gen_pairs: List[Tuple[str, str]],
    ) -> None:
        """Evaluate batch translation on formal sentences from IN22-gen benchmark."""
        assert len(in22_gen_pairs) >= 3

        batch_sources = [pair[0] for pair in in22_gen_pairs[:3]]
        results = translation_engine.translate_batch(
            texts=batch_sources,
            source_language="hi",
            target_language="sat",
        )

        assert len(results) == 3
        for res, orig_src in zip(results, batch_sources):
            assert res.source_text == orig_src
            assert res.source_language == "hi"
            assert res.target_language == "sat"
            assert TextNormalizer.contains_ol_chiki(res.target_text)

    def test_round_trip_conversation(
        self,
        translation_engine: TranslationInterface,
    ) -> None:
        """Test round-trip translation: Hindi -> Santali Ol Chiki -> Hindi Devanagari."""
        original_hi = "नमस्ते, आप कैसे हैं?"

        # Forward hop: Hindi -> Santali
        fwd_res = translation_engine.translate(original_hi, "hi", "sat")
        assert TextNormalizer.contains_ol_chiki(fwd_res.target_text)

        # Reverse hop: Santali -> Hindi
        rev_res = translation_engine.translate(fwd_res.target_text, "sat", "hi")
        assert TextNormalizer.contains_devanagari(rev_res.target_text)

    def test_empty_string_handling(
        self,
        translation_engine: TranslationInterface,
    ) -> None:
        """Ensure empty input string produces clean empty output without errors."""
        res = translation_engine.translate("", "hi", "sat")
        assert res.target_text == ""
        assert res.processing_time_ms == 0.0
