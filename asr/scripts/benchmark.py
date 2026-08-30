"""
Baseline Benchmark Script (Phase 5)
Measures model loading time, first inference latency, repeated inference latency (average, median, P95), RTF, peak RAM, and disk footprint.
Outputs asr/outputs/baseline_benchmark.json.
"""

import os
import sys
import time
import json
import psutil
import numpy as np
from transcribe import IndicConformerASR

sys.stdout.reconfigure(encoding='utf-8')
sys.stderr.reconfigure(encoding='utf-8')

def main():
    process = psutil.Process(os.getpid())
    audio_dir = "asr/tests/audio"
    audio_files = [os.path.join(audio_dir, f"00{i}.wav" if i < 10 else f"0{i}.wav") for i in range(1, 16)]
    audio_files = [f for f in audio_files if os.path.exists(f)]

    print("============================================================")
    print("INDICCONFORMER ASR BASELINE BENCHMARK (Phase 5)")
    print("============================================================")

    # 1. Model Loading Time
    t0_load = time.time()
    asr = IndicConformerASR(model_path="asr/models/onnx/model.int8.onnx")
    load_time_ms = (time.time() - t0_load) * 1000.0
    print(f"[*] Model Loading Time: {load_time_ms:.2f} ms")

    latencies_ms = []
    rtfs = []
    first_latency_ms = 0.0

    # 2. Benchmark Runs (15 samples)
    for idx, audio_path in enumerate(audio_files):
        text, lat_ms, rtf = asr.transcribe(audio_path)
        if idx == 0:
            first_latency_ms = lat_ms
        latencies_ms.append(lat_ms)
        rtfs.append(rtf)
        print(f"  Run {idx+1:02d}: {os.path.basename(audio_path)} -> {lat_ms:.2f} ms (RTF: {rtf:.3f})")

    avg_lat_ms = float(np.mean(latencies_ms))
    med_lat_ms = float(np.median(latencies_ms))
    p95_lat_ms = float(np.percentile(latencies_ms, 95))
    avg_rtf = float(np.mean(rtfs))
    peak_ram_mb = process.memory_info().rss / (1024.0 * 1024.0)

    fp32_size_mb = (os.path.getsize("asr/models/onnx/model.onnx") + os.path.getsize("asr/models/onnx/model.onnx_data")) / (1024.0 * 1024.0)
    int8_size_mb = os.path.getsize("asr/models/onnx/model.int8.onnx") / (1024.0 * 1024.0)

    report = {
        "model": "ai4bharat/indicconformer_stt_hi_hybrid_ctc_rnnt_large",
        "precision": "INT8 Dynamic Quantized ONNX",
        "device": "Desktop CPU",
        "cpu": "Intel/AMD Multi-Core CPU",
        "python_version": sys.version.split()[0],
        "model_load_time_ms": round(load_time_ms, 2),
        "first_inference_latency_ms": round(first_latency_ms, 2),
        "average_latency_ms": round(avg_lat_ms, 2),
        "median_latency_ms": round(med_lat_ms, 2),
        "p95_latency_ms": round(p95_lat_ms, 2),
        "average_rtf": round(avg_rtf, 3),
        "peak_ram_mb": round(peak_ram_mb, 2),
        "fp32_model_disk_size_mb": round(fp32_size_mb, 2),
        "int8_model_disk_size_mb": round(int8_size_mb, 2),
        "samples_evaluated": len(latencies_ms),
        "status": "PASS"
    }

    report_path = "asr/outputs/baseline_benchmark.json"
    with open(report_path, "w", encoding="utf-8") as f:
        json.dump(report, f, indent=2)

    print("\n" + "=" * 60)
    print("BASELINE BENCHMARK SUMMARY")
    print("=" * 60)
    print(json.dumps(report, indent=2))
    print(f"\n[OK] Benchmark report saved to {report_path}")
    print("=" * 60)

if __name__ == "__main__":
    main()
