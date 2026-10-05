"""Driver: Qwen 0.5B INT8 -> INT4 weight packing + numeric validation.

Long-running (488 MB model); run detached:
    Start-Process python -ArgumentList 'scripts/run_phase2_qwen.py' -RedirectStandardOutput 'scripts/qwen_int4.log' -RedirectStandardError 'scripts/qwen_int4.err' -NoNewWindow
"""
from __future__ import annotations

import json
import logging
import sys
import time
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
logging.getLogger().setLevel(logging.ERROR)

import onnxruntime as ort  # noqa: E402

ort.set_default_logger_severity(4)

from quantize_models import REPORT, STAGED, mb, quantize_qwen_int4  # noqa: E402


def main() -> None:
    src = STAGED / "qwen" / "model_int8.onnx"
    dst = STAGED / "qwen" / "model_int4.onnx"
    print(f"--- quantizing qwen ({mb(src)} MB) -> INT4 ---", flush=True)
    t0 = time.time()
    res = quantize_qwen_int4(src, dst)
    print(f"qwen: {res['src_mb']} MB -> {res['dst_mb']} MB | "
          f"max_abs_diff={res['max_abs_diff']:.4f} | "
          f"argmax_agrees={res['argmax_agrees']} | "
          f"{res['seconds']}s", flush=True)
    report_path = REPORT.parent / "quantization_report_qwen.json"
    report_path.write_text(json.dumps({"qwen": res}, indent=2))
    print(f"REPORT saved to {report_path}", flush=True)
    print(f"TOTAL elapsed {time.time()-t0:.0f}s", flush=True)


if __name__ == "__main__":
    main()
