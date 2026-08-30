"""
Hindi -> Santali Translation Latency & Memory Benchmark Script (Phases 5 & 6)
Measures model load time, first inference latency, sentence latency distribution (avg, median, P95), throughput, and RAM usage.
Updates translation/results/metrics.json.
"""

import os
import sys
import csv
import json
import time
import psutil
import torch
import numpy as np

sys.stdout.reconfigure(encoding='utf-8')
sys.stderr.reconfigure(encoding='utf-8')

import transformers.tokenization_utils_base
import transformers.tokenization_utils
transformers.tokenization_utils.PreTrainedTokenizerBase = transformers.tokenization_utils_base.PreTrainedTokenizerBase

sys.path.append("translation/models")

from translate import IndicTransONNX

DEFAULT_CSV_PATH = r"C:\Users\jesva\Downloads\translation-data\hi_sat_in22.csv"
DEFAULT_METRICS_JSON = "translation/results/metrics.json"

def main():
    csv_path = sys.argv[1] if len(sys.argv) > 1 else DEFAULT_CSV_PATH
    json_path = sys.argv[2] if len(sys.argv) > 2 else DEFAULT_METRICS_JSON

    process = psutil.Process(os.getpid())
    baseline_ram_mb = process.memory_info().rss / (1024.0 * 1024.0)

    print("============================================================")
    print("BENCHMARKING INDIC TRANS2 HINDI -> SANTALI TRANSLATION")
    print("============================================================")
    print(f"[*] Baseline RAM  : {baseline_ram_mb:.2f} MB")
    print(f"[*] Input CSV     : {csv_path}")

    if not os.path.exists(csv_path):
        print(f"[ERROR] Input CSV not found: {csv_path}")
        return

    # Load Model
    t0_load = time.time()
    model_dir = "translation/models"
    translator = IndicTransONNX(model_dir)
    model_load_ms = (time.time() - t0_load) * 1000.0
    print(f"[OK] Model Loaded in {model_load_ms:.2f} ms")

    # Read dataset
    with open(csv_path, "r", encoding="utf-8-sig") as f:
        reader = list(csv.DictReader(f))

    total_samples = len(reader)
    hindi_texts = [r["hindi"].strip() for r in reader]

    # Benchmark First Inference Latency
    t0_first = time.time()
    translator.translate(hindi_texts[0], src_lang="hin_Deva", tgt_lang="sat_Olck")
    first_inference_ms = (time.time() - t0_first) * 1000.0
    print(f"[OK] First Inference Latency: {first_inference_ms:.2f} ms")

    # Measure Sentence Latency across subset / full dataset
    latencies_ms = []
    t0_total = time.time()

    for idx, text in enumerate(hindi_texts):
        t0 = time.time()
        translator.translate(text, src_lang="hin_Deva", tgt_lang="sat_Olck")
        lat_ms = (time.time() - t0) * 1000.0
        latencies_ms.append(lat_ms)

        if (idx + 1) % 300 == 0 or (idx + 1) == total_samples:
            print(f"  Benchmarked [{idx+1:04d}/{total_samples}] - Last Sentence: {lat_ms:.2f} ms")

    total_infer_time_ms = (time.time() - t0_total) * 1000.0
    peak_ram_mb = process.memory_info().rss / (1024.0 * 1024.0)

    avg_sent_lat_ms = float(np.mean(latencies_ms))
    med_sent_lat_ms = float(np.median(latencies_ms))
    p95_sent_lat_ms = float(np.percentile(latencies_ms, 95))
    sents_per_sec = total_samples / (total_infer_time_ms / 1000.0)

    print("\n" + "=" * 65)
    print("INDIC TRANS2 TRANSLATION BENCHMARK SUMMARY")
    print("=" * 65)
    print(f"  Total Samples           : {total_samples}")
    print(f"  Model Load Time         : {model_load_ms:.2f} ms")
    print(f"  First Inference Latency : {first_inference_ms:.2f} ms")
    print(f"  Average Sentence Latency: {avg_sent_lat_ms:.2f} ms")
    print(f"  Median Sentence Latency : {med_sent_lat_ms:.2f} ms")
    print(f"  P95 Sentence Latency    : {p95_sent_lat_ms:.2f} ms")
    print(f"  Total Translation Time  : {total_infer_time_ms / 1000.0:.2f} s")
    print(f"  Throughput              : {sents_per_sec:.2f} sents/sec")
    print(f"  Baseline RAM            : {baseline_ram_mb:.2f} MB")
    print(f"  Peak RAM                : {peak_ram_mb:.2f} MB")
    print("=" * 65)

    # Load existing metrics.json if present
    metrics = {}
    if os.path.exists(json_path):
        with open(json_path, "r", encoding="utf-8") as f:
            metrics = json.load(f)

    # Update with latency & RAM measurements
    metrics.update({
        "model_load_ms": round(model_load_ms, 2),
        "first_inference_ms": round(first_inference_ms, 2),
        "average_sentence_latency_ms": round(avg_sent_lat_ms, 2),
        "median_latency_ms": round(med_sent_lat_ms, 2),
        "p95_latency_ms": round(p95_sent_lat_ms, 2),
        "total_inference_time_ms": round(total_infer_time_ms, 2),
        "sentences_per_second": round(sents_per_sec, 2),
        "baseline_ram_mb": round(baseline_ram_mb, 2),
        "peak_ram_mb": round(peak_ram_mb, 2),
        "device": "CPUExecutionProvider (Intel/AMD)",
        "python_version": sys.version.split()[0],
        "pytorch_version": torch.__version__
    })

    with open(json_path, "w", encoding="utf-8") as f:
        json.dump(metrics, f, indent=2, ensure_ascii=False)

    print(f"[OK] Updated metrics in {json_path}")

if __name__ == "__main__":
    main()
