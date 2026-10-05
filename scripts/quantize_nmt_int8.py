"""Quantize the three IndicTrans2 graphs to INT8 (weight-only dynamic).

Order matters for the 2 GB budget: the two decoder graphs are quantized
*separately* from their fp32 sources and only then fused, because
onnxruntime's quantizer does not descend into the If/subgraph structure that
merge_decoders produces. Quantizing first keeps full INT8 coverage
(~195 MB -> ~100 MB per decoder) and fusing second halves the decoder
*session* count, which is what actually costs RAM on device: each session
maps its own copy of the 193.6 MB shared weight file.

    python scripts/quantize_nmt_int8.py
"""

from __future__ import annotations

import sys
import time
from pathlib import Path

REPO = Path(__file__).resolve().parent.parent
NMT = REPO / "staged_models" / "nmt"

sys.path.insert(0, str(Path(__file__).resolve().parent))
from quantize_models import quantize_nmt_graph  # noqa: E402

GRAPHS = ["encoder_model", "decoder_model", "decoder_with_past_model"]


def main() -> int:
    for name in GRAPHS:
        src = NMT / f"{name}.onnx"
        dst = NMT / f"{name}_int8.onnx"
        if not src.exists():
            print(f"FAIL: {src} missing", file=sys.stderr)
            return 1
        if dst.exists() and dst.stat().st_mtime > src.stat().st_mtime:
            print(
                f"SKIP {name}: {dst.name} already current "
                f"({dst.stat().st_size / 1048576:.1f} MB)",
                flush=True,
            )
            continue
        t0 = time.time()
        info = quantize_nmt_graph(src, dst)
        print(
            f"OK   {name}: {info['src_mb']} MB -> {info['dst_mb']} MB in "
            f"{time.time() - t0:.0f}s",
            flush=True,
        )
    print("quantization complete", flush=True)
    return 0


if __name__ == "__main__":
    sys.exit(main())