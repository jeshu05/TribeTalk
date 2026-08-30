"""
SPRING_F5 INT8 Quantization Script (Phase 7)
Applies dynamic INT8 quantization using ONNX Runtime tooling to shrink model weights for low-footprint deployment.
"""

import os
import sys
import time
import json
import psutil
import onnx
import onnxruntime as ort
from onnxruntime.quantization import quantize_dynamic, QuantType

sys.stdout.reconfigure(encoding='utf-8')
sys.stderr.reconfigure(encoding='utf-8')

def quantize_component(input_path, output_path):
    fp32_mb = os.path.getsize(input_path) / 1024 / 1024
    print(f"[*] Quantizing FP32 model: {input_path} ({fp32_mb:.2f} MB)...")
    model = onnx.load(input_path)
    quantize_dynamic(
        model_input=model,
        model_output=output_path,
        weight_type=QuantType.QUInt8
    )
    int8_mb = os.path.getsize(output_path) / 1024 / 1024
    print(f"[OK] Saved INT8 model: {output_path} ({int8_mb:.2f} MB)")
    return int8_mb

def main():
    fp32_dir = "spring-f5-android/models/fp32"
    fp16_dir = "spring-f5-android/models/fp16"
    int8_dir = "spring-f5-android/models/int8"
    os.makedirs(int8_dir, exist_ok=True)

    print("============================================================")
    print("SPRING_F5 INT8 DYNAMIC QUANTIZATION (Phase 7)")
    print("============================================================")

    # 1. Quantize Transformer
    transformer_fp32 = os.path.join(fp32_dir, "spring_f5_transformer.onnx")
    transformer_int8 = os.path.join(int8_dir, "spring_f5_transformer.onnx")
    trans_int8_mb = quantize_component(transformer_fp32, transformer_int8)

    # 2. Quantize Decoder
    decoder_fp32 = os.path.join(fp32_dir, "spring_f5_decoder.onnx")
    decoder_int8 = os.path.join(int8_dir, "spring_f5_decoder.onnx")
    dec_int8_mb = quantize_component(decoder_fp32, decoder_int8)

    trans_fp32_mb = os.path.getsize(transformer_fp32) / 1024 / 1024
    dec_fp32_mb = os.path.getsize(decoder_fp32) / 1024 / 1024
    total_fp32_mb = trans_fp32_mb + dec_fp32_mb

    trans_fp16_mb = os.path.getsize(os.path.join(fp16_dir, "spring_f5_transformer.onnx")) / 1024 / 1024
    dec_fp16_mb = os.path.getsize(os.path.join(fp16_dir, "spring_f5_decoder.onnx")) / 1024 / 1024
    total_fp16_mb = trans_fp16_mb + dec_fp16_mb

    total_int8_mb = trans_int8_mb + dec_int8_mb
    reduction_pct = (1.0 - (total_int8_mb / total_fp32_mb)) * 100.0

    comparison_report = {
        "quantization_success": True,
        "precision_comparison": {
            "FP32": {
                "transformer_mb": round(trans_fp32_mb, 2),
                "decoder_mb": round(dec_fp32_mb, 2),
                "total_mb": round(total_fp32_mb, 2)
            },
            "FP16": {
                "transformer_mb": round(trans_fp16_mb, 2),
                "decoder_mb": round(dec_fp16_mb, 2),
                "total_mb": round(total_fp16_mb, 2)
            },
            "INT8": {
                "transformer_mb": round(trans_int8_mb, 2),
                "decoder_mb": round(dec_int8_mb, 2),
                "total_mb": round(total_int8_mb, 2)
            }
        },
        "size_reduction_from_fp32_pct": round(reduction_pct, 2),
        "status": "PASS"
    }

    report_path = "spring-f5-android/outputs/quantization_comparison.json"
    with open(report_path, "w", encoding="utf-8") as f:
        json.dump(comparison_report, f, indent=2)

    print("\n" + "=" * 65)
    print("FP32 vs FP16 vs INT8 PRECISION COMPARISON MATRIX")
    print("=" * 65)
    print(f"{'Precision':<10} | {'Transformer (MB)':<18} | {'Decoder (MB)':<14} | {'Total Disk Size':<15}")
    print("-" * 65)
    print(f"{'FP32':<10} | {trans_fp32_mb:<18.2f} | {dec_fp32_mb:<14.2f} | {total_fp32_mb:.2f} MB")
    print(f"{'FP16':<10} | {trans_fp16_mb:<18.2f} | {dec_fp16_mb:<14.2f} | {total_fp16_mb:.2f} MB")
    print(f"{'INT8':<10} | {trans_int8_mb:<18.2f} | {dec_int8_mb:<14.2f} | {total_int8_mb:.2f} MB")
    print("=" * 65)
    print(f"[OK] Comparison report saved to {report_path}")

if __name__ == "__main__":
    main()
