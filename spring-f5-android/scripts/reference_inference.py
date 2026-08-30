"""
SPRING_F5 PyTorch Reference Inference Script (Phase 3)
Executes deterministic inference on SPRINGLab/SPRING_F5 pretrained weights and measures performance metrics.
"""

import sys
import os

sys.stdout.reconfigure(encoding='utf-8')
sys.stderr.reconfigure(encoding='utf-8')
import time
import argparse
import psutil
import soundfile as sf
import torch
import numpy as np

# Add project root to sys.path
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from f5_tts.model.backbones.dit import DiT
from f5_tts.infer.utils_infer import (
    load_model,
    load_vocoder,
    infer_process,
    preprocess_ref_audio_text
)

def parse_args():
    parser = argparse.ArgumentParser(description="SPRING_F5 PyTorch Reference Inference")
    parser.add_argument("--text", type=str, default="ᱤᱧᱟᱜ ᱧᱩᱛᱩᱢ ᱫᱚ ᱵᱤᱨᱥᱟ ᱠᱟᱱᱟ", help="Target text to synthesize")
    parser.add_argument("--ref_audio", type=str, default=None, help="Reference audio file path")
    parser.add_argument("--ref_text", type=str, default="Hello welcome to class", help="Reference audio transcript")
    parser.add_argument("--output", type=str, default="spring-f5-android/outputs/reference.wav", help="Output WAV filepath")
    parser.add_argument("--ckpt_path", type=str, default="spring-f5-android/checkpoints/checkpoints/model_170000.pt")
    parser.add_argument("--vocab_path", type=str, default="spring-f5-android/checkpoints/checkpoints/vocab.txt")
    parser.add_argument("--n_steps", type=int, default=16, help="NFE steps for CFM Euler solver")
    return parser.parse_args()

def main():
    args = parse_args()
    os.makedirs(os.path.dirname(args.output), exist_ok=True)
    device = "cuda" if torch.cuda.is_available() else "cpu"
    process = psutil.Process(os.getpid())

    print(f"[*] Target Device: {device} ({torch.cuda.get_device_name(0) if torch.cuda.is_available() else 'CPU'})")
    print(f"[*] Target Text: '{args.text}'")

    # 1. Measure Model Load Time
    t0_load = time.time()
    model = load_model(
        DiT,
        dict(dim=1024, depth=22, heads=16, ff_mult=2, text_dim=512, conv_layers=4),
        args.ckpt_path,
        mel_spec_type="vocos",
        vocab_file=args.vocab_path,
        use_ema=True,
        device=device
    )
    vocos = load_vocoder("vocos", is_local=False, device=device)
    load_time_sec = time.time() - t0_load

    # 2. Reference audio setup (create a synthetic 3-sec 24kHz reference prompt if none provided)
    ref_audio_file = args.ref_audio
    if ref_audio_file is None or not os.path.exists(ref_audio_file):
        dummy_ref = os.path.join(os.path.dirname(args.output), "temp_ref.wav")
        sr_ref = 24000
        dummy_wave = np.sin(2 * np.pi * 440 * np.linspace(0, 3, 3 * sr_ref)).astype(np.float32) * 0.3
        sf.write(dummy_ref, dummy_wave, sr_ref)
        ref_audio_file = dummy_ref

    ref_audio, ref_text = preprocess_ref_audio_text(ref_audio_file, args.ref_text)

    # 3. Measure Inference Time
    t0_infer = time.time()
    wav, sr, _ = infer_process(
        ref_audio_file,
        ref_text,
        args.text,
        model,
        vocos,
        mel_spec_type="vocos",
        nfe_step=args.n_steps,
        cfg_strength=2.0,
        sway_sampling_coef=-1.0,
        speed=1.0,
        device=device
    )
    infer_time_sec = time.time() - t0_infer

    # 4. Save Output Audio
    sf.write(args.output, wav, sr)
    audio_duration_sec = len(wav) / sr
    rtf = infer_time_sec / audio_duration_sec if audio_duration_sec > 0 else 0
    peak_ram_mb = process.memory_info().rss / (1024 * 1024)

    print("\n" + "=" * 60)
    print("SPRING_F5 PYTORCH REFERENCE INFERENCE REPORT")
    print("=" * 60)
    print(f"  Model Load Time     : {load_time_sec:.3f} s ({load_time_sec * 1000:.1f} ms)")
    print(f"  Inference Time      : {infer_time_sec:.3f} s ({infer_time_sec * 1000:.1f} ms)")
    print(f"  Sample Rate         : {sr} Hz")
    print(f"  Audio Duration      : {audio_duration_sec:.3f} s")
    print(f"  Real-Time Factor (RTF): {rtf:.3f}")
    print(f"  Peak Memory (RSS)   : {peak_ram_mb:.2f} MB")
    print(f"  Output Saved To     : {args.output}")
    print("=" * 60)

if __name__ == "__main__":
    main()
