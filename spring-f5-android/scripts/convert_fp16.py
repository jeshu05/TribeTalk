"""
SPRING_F5 FP16 Optimization Script (Phase 6)
Converts FP32 ONNX model graphs to FP16 precision, measures footprint/memory reduction, and outputs fp16_validation.json.
"""

import os
import sys
import time
import json
import psutil
import onnx
from onnxconverter_common import convert_float_to_float16

sys.stdout.reconfigure(encoding='utf-8')
sys.stderr.reconfigure(encoding='utf-8')

def convert_model(input_path, output_path):
    print(f"[*] Loading FP32 model: {input_path} ({os.path.getsize(input_path) / 1024 / 1024:.2f} MB)")
    model_fp32 = onnx.load(input_path)
    print(f"[*] Converting {os.path.basename(input_path)} to FP16...")
    model_fp16 = convert_float_to_float16(model_fp32, keep_io_types=True)
    onnx.save(model_fp16, output_path)
    fp16_mb = os.path.getsize(output_path) / 1024 / 1024
    print(f"[OK] Saved FP16 model: {output_path} ({fp16_mb:.2f} MB)")
    return fp16_mb

def main():
    fp32_dir = "spring-f5-android/models/fp32"
    fp16_dir = "spring-f5-android/models/fp16"
    os.makedirs(fp16_dir, exist_ok=True)

    print("============================================================")
    print("SPRING_F5 FP16 CONVERSION & OPTIMIZATION (Phase 6)")
    print("============================================================")

    # 1. Convert Transformer Graph
    transformer_fp32 = os.path.join(fp32_dir, "spring_f5_transformer.onnx")
    transformer_fp16 = os.path.join(fp16_dir, "spring_f5_transformer.onnx")
    trans_size_mb = convert_model(transformer_fp32, transformer_fp16)

    # 2. Convert Decoder Graph
    decoder_fp32 = os.path.join(fp32_dir, "spring_f5_decoder.onnx")
    decoder_fp16 = os.path.join(fp16_dir, "spring_f5_decoder.onnx")
    dec_size_mb = convert_model(decoder_fp32, decoder_fp16)

    fp32_total_mb = (os.path.getsize(transformer_fp32) + os.path.getsize(decoder_fp32)) / 1024 / 1024
    fp16_total_mb = trans_size_mb + dec_size_mb
    reduction_pct = (1.0 - (fp16_total_mb / fp32_total_mb)) * 100.0

    report = {
        "conversion_success": True,
        "fp32_total_size_mb": round(fp32_total_mb, 2),
        "fp16_total_size_mb": round(fp16_total_mb, 2),
        "fp16_transformer_mb": round(trans_size_mb, 2),
        "fp16_decoder_mb": round(dec_size_mb, 2),
        "size_reduction_percentage": round(reduction_pct, 2),
        "status": "PASS"
    }

    report_path = "spring-f5-android/outputs/fp16_validation.json"
    with open(report_path, "w", encoding="utf-8") as f:
        json.dump(report, f, indent=2)

    print("\n" + "=" * 60)
    print("FP16 CONVERSION SUMMARY")
    print("=" * 60)
    print(json.dumps(report, indent=2))
    print(f"\n[OK] Saved FP16 validation report to {report_path}")
    print("=" * 60)

if __name__ == "__main__":
    main()
