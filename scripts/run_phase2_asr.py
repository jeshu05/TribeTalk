"""Driver: quantize both ASR conformers + report. Part of Phase 2 model diet."""
from __future__ import annotations

import logging
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

logging.getLogger().setLevel(logging.ERROR)  # silence quantizer shape-inference warnings

import onnxruntime as ort

ort.set_default_logger_severity(4)  # suppress CleanUnusedInitializers noise

from quantize_models import REPORT, STAGED, mb, quantize_asr  # noqa: E402

import json  # noqa: E402


def main() -> None:
    res = {}
    for lang in ["hindi", "santali"]:
        src = STAGED / "asr" / f"{lang}_conformer.onnx"
        dst = STAGED / "asr" / f"{lang}_conformer_int8.onnx"
        print(f"--- quantizing {lang} ({mb(src)} MB) ---", flush=True)
        res[lang] = quantize_asr(src, dst)
        print(f"{lang}: {res[lang]['src_mb']} MB -> {res[lang]['dst_mb']} MB | "
              f"top1_agreement_min={res[lang]['timestep_top1_agreement_min']*100:.2f}% | "
              f"ctc_identical={res[lang]['ctc_decode_identical_all_trials']} | "
              f"renamed={res[lang]['renamed_duplicate_edges']} | "
              f"{res[lang]['seconds']}s", flush=True)
    REPORT.write_text(json.dumps({"asr": res}, indent=2))
    print(f"REPORT saved to {REPORT}", flush=True)


if __name__ == "__main__":
    main()
