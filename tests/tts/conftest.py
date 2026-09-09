"""Pytest fixtures for TTS evaluation using tribe-evaluation IndicVoices-R benchmarks."""

from pathlib import Path
from typing import List
import pytest
import pyarrow.parquet as pq

from tribetalk.tts import get_tts, TTSInterface

TRIBE_EVAL_TTS_BASE = Path("tribe-evaluation/tts/indicvoices-r").resolve()


@pytest.fixture(scope="session")
def indicvoices_r_hindi_sentences() -> List[str]:
    """Load reference text sentences from IndicVoices-R Hindi benchmark parquet."""
    hi_pq = TRIBE_EVAL_TTS_BASE / "hindi" / "Hindi" / "test-00000-of-00002.parquet"
    if not hi_pq.exists():
        pytest.skip(f"IndicVoices-R Hindi parquet missing at: {hi_pq}")

    table = pq.read_table(str(hi_pq), columns=["text"])
    texts = [t.as_py().strip() for t in table["text"] if t.as_py() and len(t.as_py().strip()) > 10]
    return texts[:10]


@pytest.fixture(scope="session")
def indicvoices_r_santali_sentences() -> List[str]:
    """Load reference text sentences from IndicVoices-R Santali benchmark parquet."""
    sat_pq = TRIBE_EVAL_TTS_BASE / "santali" / "Santali" / "test-00000-of-00002.parquet"
    if not sat_pq.exists():
        pytest.skip(f"IndicVoices-R Santali parquet missing at: {sat_pq}")

    table = pq.read_table(str(sat_pq), columns=["text"])
    texts = [t.as_py().strip() for t in table["text"] if t.as_py() and len(t.as_py().strip()) > 5]
    return texts[:10]


@pytest.fixture(scope="session")
def shared_hindi_tts() -> TTSInterface:
    """Session-scoped Hindi TTS engine to avoid redundant model weight loads."""
    engine = get_tts("hi", lazy_load=False)
    yield engine
    engine.unload_model()


@pytest.fixture(scope="session")
def shared_santali_tts() -> TTSInterface:
    """Session-scoped Santali TTS engine to avoid redundant session initialization."""
    engine = get_tts("sat", lazy_load=False)
    yield engine
    engine.unload_model()


@pytest.fixture
def temp_wav_output(tmp_path: Path) -> Path:
    """Temporary WAV output path."""
    return tmp_path / "test_tts_out.wav"
