"""Integration tests for genuine Hindi and Santali speech synthesis.

Evaluates TTS performance and audio quality directly using sentences loaded from
the frozen tribe-evaluation IndicVoices-R benchmark parquets.
"""

from pathlib import Path
from typing import List
import pytest
import numpy as np
import soundfile as sf

from tribetalk.tts import TTSInterface, TTSResult


@pytest.mark.integration
class TestHindiTTSInference:
    """Integration test suite for Meta MMS Hindi VITS speech synthesis."""

    def test_hindi_speech_synthesis_indicvoices(
        self,
        shared_hindi_tts: TTSInterface,
        indicvoices_r_hindi_sentences: List[str],
    ) -> None:
        """Synthesize genuine Hindi sentences from IndicVoices-R benchmark."""
        assert len(indicvoices_r_hindi_sentences) >= 2

        for i in range(2):
            sentence = indicvoices_r_hindi_sentences[i]
            res = shared_hindi_tts.synthesize(sentence)

            assert isinstance(res, TTSResult)
            assert res.language == "hi"
            assert res.sample_rate == 16000
            assert res.duration_ms > 500.0
            assert res.processing_time_ms > 0.0
            assert len(res.audio) > 8000
            assert res.is_final is True

            # Audio quality assertions: non-silent, normalized
            max_amp = float(np.max(np.abs(res.audio)))
            assert max_amp > 0.05, f"Audio is silent or too quiet (max amp: {max_amp})"
            assert max_amp <= 1.0, f"Audio exceeds float32 range (max amp: {max_amp})"

            # Real-time factor assertion (must be faster than real time on CPU)
            assert res.rtf < 1.0, f"Expected RTF < 1.0, got: {res.rtf}"

    def test_hindi_save_to_wav(
        self,
        shared_hindi_tts: TTSInterface,
        temp_wav_output: Path,
    ) -> None:
        """Test direct synthesis and persistence to 16 kHz WAV file."""
        text = "नमस्ते, आपका स्वागत है।"
        out_path = shared_hindi_tts.save_to_wav(text, temp_wav_output)

        assert out_path.exists()
        assert out_path.is_file()

        data, sr = sf.read(str(out_path), dtype="float32")
        assert sr == 16000
        assert len(data) > 8000
        assert np.max(np.abs(data)) > 0.05

    def test_empty_text_raises_value_error(
        self,
        shared_hindi_tts: TTSInterface,
    ) -> None:
        with pytest.raises(ValueError, match="cannot be empty"):
            shared_hindi_tts.synthesize("")


@pytest.mark.integration
class TestSantaliTTSInference:
    """Integration test suite for Piper Santali Ol Chiki ONNX speech synthesis."""

    def test_santali_speech_synthesis_indicvoices(
        self,
        shared_santali_tts: TTSInterface,
        indicvoices_r_santali_sentences: List[str],
    ) -> None:
        """Synthesize genuine Santali Ol Chiki sentences from IndicVoices-R benchmark."""
        assert len(indicvoices_r_santali_sentences) >= 2

        for i in range(2):
            sentence = indicvoices_r_santali_sentences[i]
            res = shared_santali_tts.synthesize(sentence)

            assert isinstance(res, TTSResult)
            assert res.language == "sat"
            assert res.sample_rate == 16000
            assert res.duration_ms > 200.0
            assert res.processing_time_ms > 0.0
            assert len(res.audio) > 3000
            assert res.is_final is True

            # Audio quality assertions: non-silent, normalized
            max_amp = float(np.max(np.abs(res.audio)))
            assert max_amp > 0.05, f"Audio is silent or too quiet (max amp: {max_amp})"
            assert max_amp <= 1.0, f"Audio exceeds float32 range (max amp: {max_amp})"

            # Real-time factor assertion (must be significantly faster than real time)
            assert res.rtf < 0.5, f"Expected RTF < 0.5, got: {res.rtf}"

    def test_santali_save_to_wav(
        self,
        shared_santali_tts: TTSInterface,
        temp_wav_output: Path,
    ) -> None:
        """Test direct synthesis and persistence of Ol Chiki text to 16 kHz WAV file."""
        text = "ᱡᱚᱦᱟᱨ, ᱪᱮᱫ ᱞᱮᱠᱟ ᱢᱮᱱᱟᱜ ᱵᱤᱱᱟ?"
        out_path = shared_santali_tts.save_to_wav(text, temp_wav_output)

        assert out_path.exists()
        assert out_path.is_file()

        data, sr = sf.read(str(out_path), dtype="float32")
        assert sr == 16000
        assert len(data) > 8000
        assert np.max(np.abs(data)) > 0.05

    def test_empty_text_raises_value_error(
        self,
        shared_santali_tts: TTSInterface,
    ) -> None:
        with pytest.raises(ValueError, match="cannot be empty"):
            shared_santali_tts.synthesize("")

    def test_model_reuse_across_successive_calls(
        self,
        shared_santali_tts: TTSInterface,
    ) -> None:
        """Verify model remains resident and reuses session across successive calls."""
        res1 = shared_santali_tts.synthesize("ᱡᱚᱦᱟᱨ")
        res2 = shared_santali_tts.synthesize("ᱜᱟᱯᱟ ᱪᱟᱞᱟᱜ-ᱟ")
        assert res1.duration_ms > 0
        assert res2.duration_ms > 0
        assert shared_santali_tts.is_loaded is True
