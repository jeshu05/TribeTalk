"""Pytest fixtures for translation evaluation against tribe-evaluation benchmarks."""

from pathlib import Path
from typing import List, Tuple
import pytest

from tribetalk.translation import get_translator, TranslationInterface

# Path to the frozen tribe-evaluation benchmark directory
TRIBE_EVAL_BASE = Path("tribe-evaluation/translation").resolve()


@pytest.fixture(scope="session")
def in22_conv_pairs() -> List[Tuple[str, str]]:
    """Load reference sentence pairs from tribe-evaluation IN22-conv benchmark.

    Returns:
        List of (hindi_text, santali_text) tuples.
    """
    conv_dir = TRIBE_EVAL_BASE / "in22-conv" / "IN22_benchmark" / "IN22_benchmark" / "conv"
    hin_path = conv_dir / "test.hin_Deva"
    sat_path = conv_dir / "test.sat_Olck"

    if not hin_path.exists() or not sat_path.exists():
        pytest.skip(f"tribe-evaluation benchmark files missing at {conv_dir}")

    pairs: List[Tuple[str, str]] = []
    with open(hin_path, "r", encoding="utf-8") as f_h, open(sat_path, "r", encoding="utf-8") as f_s:
        for h_line, s_line in zip(f_h, f_s):
            h_text = h_line.strip()
            s_text = s_line.strip()
            if h_text and s_text:
                pairs.append((h_text, s_text))

    return pairs


@pytest.fixture(scope="session")
def in22_gen_pairs() -> List[Tuple[str, str]]:
    """Load reference sentence pairs from tribe-evaluation IN22-gen benchmark.

    Returns:
        List of (hindi_text, santali_text) tuples.
    """
    gen_dir = TRIBE_EVAL_BASE / "in22-gen" / "IN22_benchmark" / "IN22_benchmark" / "gen"
    hin_path = gen_dir / "test.hin_Deva"
    sat_path = gen_dir / "test.sat_Olck"

    if not hin_path.exists() or not sat_path.exists():
        pytest.skip(f"tribe-evaluation benchmark files missing at {gen_dir}")

    pairs: List[Tuple[str, str]] = []
    with open(hin_path, "r", encoding="utf-8") as f_h, open(sat_path, "r", encoding="utf-8") as f_s:
        for h_line, s_line in zip(f_h, f_s):
            h_text = h_line.strip()
            s_text = s_line.strip()
            if h_text and s_text:
                pairs.append((h_text, s_text))

    return pairs


@pytest.fixture(scope="session")
def translation_engine() -> TranslationInterface:
    """Session-scoped TranslationInterface to avoid reloading weights repeatedly."""
    engine = get_translator(lazy_load=False)
    yield engine
    engine.unload_model()
