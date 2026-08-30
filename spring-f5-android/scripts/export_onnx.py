"""
SPRING_F5 Modular ONNX Exporter Script (Phase 4)
Exports SPRINGLab/SPRING_F5 DiT backbone and Vocos decoder into dynamic, CPU-compatible ONNX graphs.
"""

import os
import sys
import argparse
import torch
import torch.nn as nn
from huggingface_hub import hf_hub_download

# Add project paths
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "f5_tts", "runtime", "triton_trtllm", "scripts")))

from f5_tts.model.backbones.dit import DiT
from f5_tts.infer.utils_infer import load_model, load_vocoder
from vocos import Vocos
try:
    from conv_stft import STFT
except ImportError:
    stft_script = hf_hub_download('SPRINGLab/SPRING_F5', 'f5_tts/runtime/triton_trtllm/scripts/conv_stft.py', local_dir='spring-f5-android')
    sys.path.append(os.path.dirname(stft_script))
    from conv_stft import STFT

class ISTFTHead(nn.Module):
    def __init__(self, original_head):
        super().__init__()
        self.out = original_head.out
        self.stft = STFT(fft_len=1024, win_hop=256, win_len=1024)

    def forward(self, x: torch.Tensor):
        x = self.out(x).transpose(1, 2)
        mag, p = x.chunk(2, dim=1)
        mag = torch.exp(mag)
        mag = torch.clip(mag, max=100.0)
        real = mag * torch.cos(p)
        imag = mag * torch.sin(p)
        audio = self.stft.inverse(real, imag)
        return audio

class VocosDecoder(nn.Module):
    def __init__(self, vocos):
        super().__init__()
        self.backbone = vocos.backbone
        self.head = ISTFTHead(vocos.head)

    def forward(self, mel):
        x = self.backbone(mel)
        return self.head(x)

class DiTWrapper(nn.Module):
    def __init__(self, transformer):
        super().__init__()
        self.transformer = transformer

    def forward(self, x, cond, text, time, mask):
        return self.transformer(x, cond, text, time, mask=mask)

def parse_args():
    parser = argparse.ArgumentParser(description="SPRING_F5 ONNX Exporter")
    parser.add_argument("--ckpt_path", type=str, default="spring-f5-android/checkpoints/checkpoints/model_170000.pt")
    parser.add_argument("--vocab_path", type=str, default="spring-f5-android/checkpoints/checkpoints/vocab.txt")
    parser.add_argument("--output_dir", type=str, default="spring-f5-android/models/fp32")
    parser.add_argument("--opset", type=int, default=17)
    return parser.parse_args()

def main():
    args = parse_args()
    os.makedirs(args.output_dir, exist_ok=True)
    device = "cpu"

    print(f"[*] Loading SPRING_F5 weights from: {args.ckpt_path}")
    model = load_model(
        DiT,
        dict(dim=1024, depth=22, heads=16, ff_mult=2, text_dim=512, conv_layers=4),
        args.ckpt_path,
        mel_spec_type="vocos",
        vocab_file=args.vocab_path,
        use_ema=True,
        device=device
    ).eval()

    vocos = load_vocoder("vocos", is_local=False, device=device).eval()

    # 1. Export Decoder
    print("\n[1/2] Exporting Vocos Decoder ONNX Graph...")
    decoder = VocosDecoder(vocos).eval()
    mel_dummy = torch.randn(1, 100, 200)
    decoder_onnx_path = os.path.join(args.output_dir, "spring_f5_decoder.onnx")

    torch.onnx.export(
        decoder, mel_dummy, decoder_onnx_path,
        input_names=["mel"], output_names=["audio"],
        dynamic_axes={"mel": {0: "batch", 2: "time"}, "audio": {0: "batch", 1: "samples"}},
        opset_version=args.opset
    )
    print(f"[OK] {decoder_onnx_path} ({os.path.getsize(decoder_onnx_path) / 1024 / 1024:.2f} MB)")

    # 2. Export Transformer
    print("\n[2/2] Exporting DiT Transformer Velocity Predictor ONNX Graph...")
    dit_wrapper = DiTWrapper(model.transformer).eval()
    b, n, d, nt = 1, 64, 100, 16
    x_dummy = torch.randn(b, n, d)
    cond_dummy = torch.randn(b, n, d)
    text_dummy = torch.randint(0, 50, (b, nt), dtype=torch.long)
    time_dummy = torch.tensor([0.5], dtype=torch.float32)
    mask_dummy = torch.ones(b, n, dtype=torch.bool)

    transformer_onnx_path = os.path.join(args.output_dir, "spring_f5_transformer.onnx")
    torch.onnx.export(
        dit_wrapper, (x_dummy, cond_dummy, text_dummy, time_dummy, mask_dummy),
        transformer_onnx_path,
        input_names=["x", "cond", "text", "time", "mask"],
        output_names=["vt"],
        dynamic_axes={
            "x": {0: "batch", 1: "seq"},
            "cond": {0: "batch", 1: "seq"},
            "text": {0: "batch", 1: "text_seq"},
            "mask": {0: "batch", 1: "seq"},
            "vt": {0: "batch", 1: "seq"}
        },
        opset_version=args.opset
    )
    print(f"[OK] {transformer_onnx_path} ({os.path.getsize(transformer_onnx_path) / 1024 / 1024:.2f} MB)")

    print("\n" + "=" * 60)
    print("SPRING_F5 MODULAR ONNX EXPORT COMPLETE")
    print("=" * 60)

if __name__ == "__main__":
    main()
