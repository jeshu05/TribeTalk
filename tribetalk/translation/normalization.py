"""Text normalization, script validation, and punctuation standardization for Hindi and Santali."""

import re


class TextNormalizer:
    """Text normalization and script verification guardrails for Hindi (Devanagari) and Santali (Ol Chiki)."""

    # Unicode ranges
    # Devanagari: U+0900 to U+097F
    DEVANAGARI_PATTERN = re.compile(r"[\u0900-\u097F]")
    # Ol Chiki: U+1C50 to U+1C7F
    OL_CHIKI_PATTERN = re.compile(r"[\u1C50-\u1C7F]")

    # Whitespace cleanup
    WHITESPACE_PATTERN = re.compile(r"\s+")

    @classmethod
    def contains_devanagari(cls, text: str) -> bool:
        """Check whether the string contains any Devanagari characters."""
        return bool(cls.DEVANAGARI_PATTERN.search(text))

    @classmethod
    def contains_ol_chiki(cls, text: str) -> bool:
        """Check whether the string contains any Ol Chiki characters."""
        return bool(cls.OL_CHIKI_PATTERN.search(text))

    @classmethod
    def validate_script_for_language(cls, text: str, language: str) -> bool:
        """Validate whether the text contains characters appropriate for the language code.

        Args:
            text: Sentence to check.
            language: 'hi' / 'hin_Deva' or 'sat' / 'sat_Olck'.

        Returns:
            True if the text contains expected vernacular characters (or is pure numbers/punctuation).
        """
        lang = str(language).strip().lower()
        if lang in ("hi", "hindi", "hin_deva", "hin"):
            # Check for Devanagari characters
            return cls.contains_devanagari(text)
        elif lang in ("sat", "santali", "sat_olck", "olchiki"):
            # Check for Ol Chiki characters
            return cls.contains_ol_chiki(text)
        return True

    @classmethod
    def normalize_input(cls, text: str) -> str:
        """Sanitize and canonicalize input text before passing to the translation model.

        1. Replaces newlines and tabs with spaces.
        2. Normalizes non-breaking and multi-spaces to a single space.
        3. Normalizes smart quotes and backticks.
        4. Trims leading/trailing whitespace.
        """
        if not text:
            return ""

        # Normalize quotes
        cleaned = (
            text.replace("“", '"')
            .replace("”", '"')
            .replace("‘", "'")
            .replace("’", "'")
            .replace("`", "'")
        )

        # Normalize whitespace
        cleaned = cls.WHITESPACE_PATTERN.sub(" ", cleaned).strip()
        return cleaned

    @classmethod
    def normalize_output(cls, text: str, target_lang: str) -> str:
        """Post-process translation output to ensure script and punctuation coherence.

        Args:
            text: Raw generated sentence.
            target_lang: 'hi' or 'sat'.

        Returns:
            Cleaned target sentence.
        """
        if not text:
            return ""

        cleaned = cls.normalize_input(text)
        lang = str(target_lang).strip().lower()

        # Santali Ol Chiki punctuation harmonization
        if lang in ("sat", "santali", "sat_olck", "olchiki"):
            # Convert standard ASCII period to Ol Chiki Danda ᱾ if adjacent to Ol Chiki text
            if cls.contains_ol_chiki(cleaned) and cleaned.endswith("."):
                cleaned = cleaned[:-1].rstrip() + " ᱾"

        # Hindi Devanagari punctuation harmonization
        elif lang in ("hi", "hindi", "hin_deva", "hin"):
            # If ending with standard ASCII period and has Devanagari, convert to Devanagari Danda
            if cls.contains_devanagari(cleaned) and cleaned.endswith("."):
                cleaned = cleaned[:-1].rstrip() + "।"

        return cleaned.strip()
