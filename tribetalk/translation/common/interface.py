"""Abstract base class defining the unified TribeTalk translation interface."""

from abc import ABC, abstractmethod
from typing import List
from tribetalk.translation.common.types import TranslationResult


class TranslationInterface(ABC):
    """Unified interface for TribeTalk neural machine translation engines.

    Supports bidirectional translation between Hindi (Devanagari) and Santali (Ol Chiki),
    with explicit lifecycle management (load and unload) for memory-constrained operation.
    """

    @property
    @abstractmethod
    def is_loaded(self) -> bool:
        """Whether model sessions and tokenizers are currently resident in memory."""
        pass

    @abstractmethod
    def load_model(self) -> None:
        """Load model sessions and tokenizers into memory.

        Must be idempotent: repeated calls should not duplicate memory allocations.
        """
        pass

    @abstractmethod
    def unload_model(self) -> None:
        """Unload model sessions, release memory, and force garbage collection.

        Must be idempotent: safe to call when already unloaded.
        """
        pass

    @abstractmethod
    def translate(
        self,
        text: str,
        source_language: str,
        target_language: str,
    ) -> TranslationResult:
        """Translate a single sentence between Hindi and Santali.

        Args:
            text: Input sentence in the source script.
            source_language: Language code or alias ('hi' or 'sat').
            target_language: Language code or alias ('sat' or 'hi').

        Returns:
            TranslationResult containing translated text and metrics.

        Raises:
            ValueError: If language codes are unsupported or identical.
            RuntimeError: If model inference fails.
        """
        pass

    @abstractmethod
    def translate_batch(
        self,
        texts: List[str],
        source_language: str,
        target_language: str,
    ) -> List[TranslationResult]:
        """Translate a batch of sentences between Hindi and Santali.

        Args:
            texts: List of input sentences.
            source_language: Language code or alias ('hi' or 'sat').
            target_language: Language code or alias ('sat' or 'hi').

        Returns:
            List of TranslationResult instances in corresponding order.
        """
        pass
