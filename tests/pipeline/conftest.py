"""Pytest fixtures for TribeTalk Pipeline & Resource Manager tests.

Loads benchmark audio and text strictly in read-only mode from tribe-evaluation/.
"""

import io
from pathlib import Path
from typing import Generator, Tuple

import numpy as np
import pyarrow.parquet as pq
import pytest
import soundfile as sf

REPO_ROOT = Path(__file__).parent.parent.parent
EVAL_DIR = REPO_ROOT / "tribe-evaluation"


@pytest.fixture
def synthetic_16k_wav(tmp_path: Path) -> Path:
    """Generate a clean 1-second 16 kHz mono WAV file."""
    wav_path = tmp_path / "synthetic_16k.wav"
    sample_rate = 16000
    duration_s = 1.0
    t = np.linspace(0, duration_s, int(duration_s * sample_rate), endpoint=False, dtype=np.float32)
    sine = 0.4 * np.sin(2 * np.pi * 440.0 * t)
    sf.write(str(wav_path), sine, sample_rate, subtype="PCM_16")
    return wav_path


@pytest.fixture(scope="session")
def hindi_eval_sample(tmp_path_factory) -> Tuple[Path, str]:
    """Extract a genuine Hindi speech sample from IndicVoices benchmark parquet."""
    pq_path = EVAL_DIR / "asr" / "hindi" / "IndicVoices_hindi" / "valid-00000-of-00001.parquet"
    if not pq_path.exists():
        pytest.skip(f"Hindi benchmark parquet missing at {pq_path}")

    pf = pq.ParquetFile(str(pq_path))
    batch = pf.read_row_group(0, columns=["audio_filepath", "normalized"])
    audio_bytes = batch["audio_filepath"][0]["bytes"].as_py()
    text = batch["normalized"][0].as_py()

    data, sr = sf.read(io.BytesIO(audio_bytes))
    # Slice to first 3 seconds for fast, focused integration testing
    max_samples = min(len(data), 3 * sr)
    data = data[:max_samples]

    out_dir = tmp_path_factory.mktemp("eval_audio")
    out_path = out_dir / "hindi_sample.wav"
    sf.write(str(out_path), data, sr, subtype="PCM_16")
    return out_path, text


@pytest.fixture(scope="session")
def santali_eval_sample(tmp_path_factory) -> Tuple[Path, str]:
    """Extract a genuine Santali speech sample from IndicVoices benchmark parquet."""
    pq_path = EVAL_DIR / "asr" / "santhali" / "IndicVoices_santhali" / "valid-00000-of-00001 (1).parquet"
    if not pq_path.exists():
        pytest.skip(f"Santali benchmark parquet missing at {pq_path}")

    pf = pq.ParquetFile(str(pq_path))
    batch = pf.read_row_group(0, columns=["audio_filepath", "normalized"])
    audio_bytes = batch["audio_filepath"][0]["bytes"].as_py()
    text = batch["normalized"][0].as_py()

    data, sr = sf.read(io.BytesIO(audio_bytes))
    # Slice to first 3 seconds for fast, focused integration testing
    max_samples = min(len(data), 3 * sr)
    data = data[:max_samples]

    out_dir = tmp_path_factory.mktemp("eval_audio")
    out_path = out_dir / "santali_sample.wav"
    sf.write(str(out_path), data, sr, subtype="PCM_16")
    return out_path, text
