"""
Hindi -> Santali IndicTrans2 Translation Pipeline (Phase 2)
Translates 1,503 Hindi sentences from hi_sat_in22.csv into Santali Ol Chiki using IndicTrans2.
Saves results to translation/results/predictions.csv.
"""

import os
import sys
import csv
import time
import argparse
import numpy as np

sys.stdout.reconfigure(encoding='utf-8')
sys.stderr.reconfigure(encoding='utf-8')

import transformers.tokenization_utils_base
import transformers.tokenization_utils
transformers.tokenization_utils.PreTrainedTokenizerBase = transformers.tokenization_utils_base.PreTrainedTokenizerBase

sys.path.append("translation/models")

from translate import IndicTransONNX

DEFAULT_CSV_PATH = r"C:\Users\jesva\Downloads\translation-data\hi_sat_in22.csv"

def parse_args():
    parser = argparse.ArgumentParser(description="IndicTrans2 Hindi -> Santali Translation")
    parser.add_argument("--csv", type=str, default=DEFAULT_CSV_PATH, help="Input CSV path")
    parser.add_argument("--model-dir", type=str, default="translation/models", help="IndicTrans2 model directory")
    parser.add_argument("--batch-size", type=int, default=8, help="Batch size for translation")
    parser.add_argument("--output", type=str, default="translation/results/predictions.csv", help="Output predictions CSV")
    return parser.parse_args()

def main():
    args = parse_args()
    if not os.path.exists(args.csv):
        print(f"[ERROR] Input CSV not found: {args.csv}")
        return

    os.makedirs(os.path.dirname(args.output), exist_ok=True)

    print("============================================================")
    print("INDICTRANS2 HINDI -> SANTALI (hin_Deva -> sat_Olck) TRANSLATION")
    print("============================================================")
    print(f"[*] Input Dataset : {args.csv}")
    print(f"[*] Model Dir     : {args.model_dir}")
    print(f"[*] Batch Size    : {args.batch_size}")
    print(f"[*] Output CSV    : {args.output}")

    # Initialize Translator
    t0_load = time.time()
    translator = IndicTransONNX(args.model_dir)
    load_time_ms = (time.time() - t0_load) * 1000.0
    print(f"[OK] Model loaded in {load_time_ms:.2f} ms")

    # Read input CSV
    rows = []
    with open(args.csv, "r", encoding="utf-8-sig") as f:
        reader = csv.DictReader(f)
        for r in reader:
            rows.append({
                "id": r["id"],
                "hindi": r["hindi"].strip(),
                "santhali": r["santhali"].strip()
            })

    total_count = len(rows)
    print(f"[*] Read {total_count} sentence pairs from CSV")

    predictions = []
    t0_infer = time.time()

    # Translate in batches
    for i in range(0, total_count, args.batch_size):
        batch_rows = rows[i:i + args.batch_size]
        for item in batch_rows:
            raw_hi = item["hindi"]
            pred_sat = translator.translate(raw_hi, src_lang="hin_Deva", tgt_lang="sat_Olck")
            predictions.append({
                "id": item["id"],
                "hindi": raw_hi,
                "reference_santhali": item["santhali"],
                "predicted_santhali": pred_sat
            })

        if (i + len(batch_rows)) % 100 == 0 or (i + len(batch_rows)) == total_count:
            elapsed = time.time() - t0_infer
            sent_per_sec = len(predictions) / elapsed if elapsed > 0 else 0
            print(f"  Processed [{len(predictions):04d}/{total_count}] - Throughput: {sent_per_sec:.2f} sent/sec")

    total_infer_time_sec = time.time() - t0_infer

    # Save to predictions.csv
    with open(args.output, "w", encoding="utf-8", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=["id", "hindi", "reference_santhali", "predicted_santhali"])
        writer.writeheader()
        writer.writerows(predictions)

    print("\n" + "=" * 60)
    print(f"[OK] Saved {len(predictions)} predictions to {args.output}")
    print(f"Total Translation Time: {total_infer_time_sec:.2f} s ({len(predictions)/total_infer_time_sec:.2f} sent/s)")
    print("=" * 60)

if __name__ == "__main__":
    main()
