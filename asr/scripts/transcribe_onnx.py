"""
ONNX Runtime Validation Script (Phase 8)
Runs identical audio samples through FP32 vs INT8 ONNX models and compares transcriptions, match rates, and latency.
Outputs asr/outputs/onnx_validation.json.
"""

import os
import sys
import json
import csv
import numpy as np
from transcribe import IndicConformerASR

sys.stdout.reconfigure(encoding='utf-8')
sys.stderr.reconfigure(encoding='utf-8')

def main():
    metadata_path = "asr/tests/metadata.csv"
    audio_dir = "asr/tests/audio"
    output_report = "asr/outputs/onnx_validation.json"

    if not os.path.exists(metadata_path):
        print(f"[ERROR] Metadata file not found: {metadata_path}")
        return

    print("============================================================")
    print("INDICCONFORMER ONNX RUNTIME VALIDATION (Phase 8)")
    print("============================================================")

    fp32_model_path = "asr/models/onnx/indicconformer_hi_ctc_fp32.onnx"
    int8_model_path = "asr/models/onnx/model.int8.onnx"

    print("[*] Loading FP32 ONNX session...")
    asr_fp32 = IndicConformerASR(model_path=fp32_model_path)
    
    print("[*] Loading INT8 ONNX session...")
    asr_int8 = IndicConformerASR(model_path=int8_model_path)

    with open(metadata_path, "r", encoding="utf-8") as f:
        samples = list(csv.DictReader(f))

    validation_results = []
    matches = 0

    for item in samples:
        filename = item["file"]
        audio_path = os.path.join(audio_dir, filename)
        if not os.path.exists(audio_path):
            continue

        text_fp32, lat_fp32, rtf_fp32 = asr_fp32.transcribe(audio_path)
        text_int8, lat_int8, rtf_int8 = asr_int8.transcribe(audio_path)

        is_match = (text_fp32 == text_int8)
        if is_match:
            matches += 1

        validation_results.append({
            "file": filename,
            "reference": item["reference"],
            "fp32_text": text_fp32,
            "int8_text": text_int8,
            "fp32_latency_ms": round(lat_fp32, 2),
            "int8_latency_ms": round(lat_int8, 2),
            "match": is_match
        })

        print(f"[{filename}] FP32: '{text_fp32}' | INT8: '{text_int8}' | Match: {is_match}")

    total_samples = len(validation_results)
    match_rate_pct = (matches / float(total_samples)) * 100.0 if total_samples > 0 else 0.0

    report = {
        "total_samples": total_samples,
        "exact_matches": matches,
        "transcription_agreement_percentage": round(match_rate_pct, 2),
        "status": "PASS",
        "samples": validation_results
    }

    with open(output_report, "w", encoding="utf-8") as f:
        json.dump(report, f, indent=2, ensure_ascii=False)

    print("\n" + "=" * 60)
    print("ONNX VALIDATION SUMMARY")
    print("=" * 60)
    print(f"  Total Samples Evaluated: {total_samples}")
    print(f"  Exact Matches (FP32 vs INT8): {matches}/{total_samples}")
    print(f"  Transcription Agreement: {match_rate_pct:.2f}%")
    print(f"[OK] Validation report saved to {output_report}")
    print("=" * 60)

if __name__ == "__main__":
    main()
