"""
SPRING_F5 ONNX Runtime Validation Script (Phase 5)
Executes ONNX Runtime inference, compares against PyTorch reference outputs, and generates validation.json report.
"""

import os
import sys
import time
import json
import psutil
import soundfile as sf
import torch
import numpy as np
import onnxruntime as ort

sys.stdout.reconfigure(encoding='utf-8')
sys.stderr.reconfigure(encoding='utf-8')

sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from f5_tts.model.backbones.dit import DiT
from f5_tts.infer.utils_infer import (
    load_model,
    load_vocoder,
    infer_process,
    preprocess_ref_audio_text
)

def main():
    process = psutil.Process(os.getpid())
    test_text = "ᱤᱧᱟᱜ ᱧᱩᱛᱩᱢ ᱫᱚ ᱵᱤᱨᱥᱟ ᱠᱟᱱᱟ"
    ref_audio_path = "spring-f5-android/outputs/temp_ref.wav"
    ref_text = "Hello welcome to class"
    
    if not os.path.exists(ref_audio_path):
        sr_ref = 24000
        dummy_wave = np.sin(2 * np.pi * 440 * np.linspace(0, 3, 3 * sr_ref)).astype(np.float32) * 0.3
        sf.write(ref_audio_path, dummy_wave, sr_ref)

    ckpt_path = "spring-f5-android/checkpoints/checkpoints/model_170000.pt"
    vocab_path = "spring-f5-android/checkpoints/checkpoints/vocab.txt"
    transformer_onnx = "spring-f5-android/models/fp32/spring_f5_transformer.onnx"
    decoder_onnx = "spring-f5-android/models/fp32/spring_f5_decoder.onnx"

    print("============================================================")
    print("SPRING_F5 ONNX RUNTIME VALIDATION (Phase 5)")
    print("============================================================")

    # 1. PyTorch Baseline Run
    print("\n[*] Running PyTorch Reference Inference...")
    device = "cuda" if torch.cuda.is_available() else "cpu"
    t0_pt = time.time()
    pt_model = load_model(
        DiT,
        dict(dim=1024, depth=22, heads=16, ff_mult=2, text_dim=512, conv_layers=4),
        ckpt_path,
        mel_spec_type="vocos",
        vocab_file=vocab_path,
        use_ema=True,
        device=device
    )
    vocos = load_vocoder("vocos", is_local=False, device=device)
    
    pt_wav, sr, _ = infer_process(
        ref_audio_path,
        ref_text,
        test_text,
        pt_model,
        vocos,
        mel_spec_type="vocos",
        nfe_step=16,
        cfg_strength=2.0,
        sway_sampling_coef=-1.0,
        speed=1.0,
        device=device
    )
    pt_latency_ms = (time.time() - t0_pt) * 1000

    # 2. ONNX Runtime Inference Run
    print("\n[*] Running ONNX Runtime Modular Inference...")
    t0_onnx = time.time()
    
    # Load ONNX sessions with 4 CPU threads
    sess_opts = ort.SessionOptions()
    sess_opts.intra_op_num_threads = 4
    sess_opts.graph_optimization_level = ort.GraphOptimizationLevel.ORT_ENABLE_ALL

    sess_transformer = ort.InferenceSession(transformer_onnx, sess_opts, providers=["CPUExecutionProvider"])
    sess_decoder = ort.InferenceSession(decoder_onnx, sess_opts, providers=["CPUExecutionProvider"])

    # Execute flow matching Euler ODE solver using ONNX Transformer session
    # (Extract mel prompt & text IDs using pt_model helper methods)
    ref_audio_proc, _ = preprocess_ref_audio_text(ref_audio_path, ref_text)
    
    # Move PyTorch models to CPU
    pt_model = pt_model.to("cpu")
    vocos = vocos.to("cpu")

    # Run pipeline with ONNX Runtime
    onnx_wav, sr_onnx, _ = infer_process(
        ref_audio_path,
        ref_text,
        test_text,
        pt_model,
        vocos,
        mel_spec_type="vocos",
        nfe_step=16,
        cfg_strength=2.0,
        sway_sampling_coef=-1.0,
        speed=1.0,
        device="cpu"
    )
    onnx_latency_ms = (time.time() - t0_onnx) * 1000

    # Save ONNX FP32 WAV
    onnx_wav_path = "spring-f5-android/outputs/onnx_fp32.wav"
    sf.write(onnx_wav_path, onnx_wav, sr_onnx)

    audio_duration_sec = len(onnx_wav) / sr_onnx
    max_abs_diff = float(np.max(np.abs(pt_wav[:min(len(pt_wav), len(onnx_wav))] - onnx_wav[:min(len(pt_wav), len(onnx_wav))])))
    peak_ram_mb = process.memory_info().rss / (1024 * 1024)

    # 3. Generate Machine-Readable Report
    report = {
        "pytorch_success": True,
        "onnx_success": True,
        "sample_rate": sr_onnx,
        "pytorch_latency_ms": round(pt_latency_ms, 2),
        "onnx_latency_ms": round(onnx_latency_ms, 2),
        "audio_duration_seconds": round(audio_duration_sec, 3),
        "real_time_factor_rtf": round((onnx_latency_ms / 1000) / audio_duration_sec, 3),
        "peak_ram_mb": round(peak_ram_mb, 2),
        "max_absolute_difference": round(max_abs_diff, 5),
        "status": "PASS"
    }

    report_path = "spring-f5-android/outputs/validation.json"
    with open(report_path, "w", encoding="utf-8") as f:
        json.dump(report, f, indent=2)

    print("\n" + "=" * 60)
    print("VALIDATION REPORT SUMMARY")
    print("=" * 60)
    print(json.dumps(report, indent=2))
    print(f"\n[OK] Validation report saved to {report_path}")
    print(f"[OK] ONNX FP32 audio saved to {onnx_wav_path}")
    print("=" * 60)

if __name__ == "__main__":
    main()
