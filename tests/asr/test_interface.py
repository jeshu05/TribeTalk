"""Unit tests for unified ASR interface and factory routing."""

import pytest
from pathlib import Path
from tribetalk.asr.common.interface import ASRInterface
from tribetalk.asr.common.types import ASRResult
from tribetalk.asr.hindi.inference import HindiASR
from tribetalk.asr.santali.inference import SantaliASR
from tribetalk.asr import get_asr


class TestASRInterfaceContract:
    """Validate abstract base class and contract inheritance."""

    def test_cannot_instantiate_abstract_interface(self) -> None:
        with pytest.raises(TypeError):
            ASRInterface()  # type: ignore

    def test_custom_subclass_must_implement_all_methods(self) -> None:
        class IncompleteRecognizer(ASRInterface):
            @property
            def language(self) -> str:
                return "test"

        with pytest.raises(TypeError):
            IncompleteRecognizer()  # type: ignore


class TestRecognizerInterfaceCompliance:
    """Ensure Hindi and Santali recognizers fulfill ASRInterface."""

    def test_hindi_recognizer_is_subclass(self) -> None:
        assert issubclass(HindiASR, ASRInterface)

    def test_santali_recognizer_is_subclass(self) -> None:
        assert issubclass(SantaliASR, ASRInterface)

    def test_hindi_recognizer_lifecycle(self) -> None:
        recognizer = HindiASR(lazy_load=True)
        assert recognizer.language == "hi"
        assert recognizer.is_loaded is False

    def test_santali_recognizer_lifecycle(self) -> None:
        recognizer = SantaliASR(lazy_load=True)
        assert recognizer.language == "sat"
        assert recognizer.is_loaded is False

    def test_hindi_transcribe_missing_file_raises(self, tmp_path: Path) -> None:
        recognizer = HindiASR(lazy_load=True)
        with pytest.raises(FileNotFoundError):
            recognizer.transcribe(tmp_path / "missing.wav")

    def test_santali_transcribe_missing_file_raises(self, tmp_path: Path) -> None:
        recognizer = SantaliASR(lazy_load=True)
        with pytest.raises(FileNotFoundError):
            recognizer.transcribe(tmp_path / "missing.wav")


class TestASRFactory:
    """Test get_asr routing and validation."""

    def test_get_asr_hindi_aliases(self) -> None:
        asr_hi = get_asr("hi", lazy_load=True)
        assert isinstance(asr_hi, HindiASR)
        assert asr_hi.language == "hi"

        asr_hindi = get_asr("HINDI", lazy_load=True)
        assert isinstance(asr_hindi, HindiASR)
        assert asr_hindi.language == "hi"

    def test_get_asr_santali_aliases(self) -> None:
        asr_sat = get_asr("sat", lazy_load=True)
        assert isinstance(asr_sat, SantaliASR)
        assert asr_sat.language == "sat"

        asr_santali = get_asr("Santali", lazy_load=True)
        assert isinstance(asr_santali, SantaliASR)
        assert asr_santali.language == "sat"

    def test_get_asr_unsupported_language_raises(self) -> None:
        with pytest.raises(ValueError, match="Unsupported ASR language"):
            get_asr("english")
        with pytest.raises(ValueError, match="Unsupported ASR language"):
            get_asr("es")
